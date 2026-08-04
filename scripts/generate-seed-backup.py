#!/usr/bin/env python3
"""Generates an encrypted backup file holding a curated demo dataset
(GitHub issues #169, #252), for seeding Play Store screenshots via the app's
own Import flow.

Replaced the previous generate-seed-sql.py after GitHub issue #212 made the
on-device Room DB SQLCipher-encrypted at rest: that script emitted raw INSERT
statements for a pipeline that pulled the sqlite file, edited it with the
host's own sqlite3 binary, and pushed it back — impossible now that the file
is ciphertext whose passphrase never leaves the app's Keystore. The backup
export/import feature uses a *separate*, caller-chosen passphrase (see
docs/BACKUP_FORMAT.md and infrastructure/security/BackupEncryptionService.kt)
independent of the DB's own key, so a script can build a plaintext
BackupFileDto JSON, encrypt it the same way the app does, and have it
imported through Settings -> Import data using a passphrase we choose.

Every date is computed relative to --today (defaults to the real current
date), so re-running later doesn't produce stale "Today" tasks or broken
streaks.

Usage: generate-seed-backup.py --locale en|es --passphrase PASS [--today YYYY-MM-DD] -o out.json
"""
import argparse
import base64
import datetime
import json
import os
from pathlib import Path

from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC
from cryptography.hazmat.primitives import hashes

SCHEMA_VERSION = 1
ENCRYPTION_VERSION = 1
KDF_NAME = "PBKDF2WithHmacSHA256"
ITERATIONS = 200_000
SALT_LENGTH_BYTES = 16
GCM_IV_LENGTH_BYTES = 12
KEY_LENGTH_BYTES = 32

LOCALES = {
    "en": {
        # Must match R.string.category_inbox for this locale — see build_plaintext.
        "inbox": "Inbox",
        "categories": ["Work", "Shopping", "Health", "Personal"],
        "tasks": [
            ("Finish Q3 budget report", "Compile numbers from finance and send to leadership by Friday."),
            ("Reply to client emails", ""),
            ("Review pull requests", "Check the two open PRs from the mobile team."),
            ("Buy groceries", "Milk, eggs, spinach, coffee beans."),
            ("Order birthday gift for Mom", ""),
            ("Pick up dry cleaning", ""),
            ("Book dentist appointment", "Six-month checkup, call before 5pm."),
            ("Refill prescription", ""),
            ("Plan weekend hike", "Check trail conditions and pack the day before."),
            ("Call Mom", ""),
            ("Read 20 pages", ""),
            ("Water the plants", ""),
            ("Renew gym membership", "The current one runs out at the end of the month."),
            ("Tidy the desk", ""),
        ],
        "habits": [
            "Drink Water", "No Smoking", "Read Before Bed", "Limit Social Media",
            "Meditate", "Morning Stretch", "Journal",
        ],
        "chain_title": "Morning Routine",
        "chain_description": "Meditate, stretch, and journal before starting the day.",
    },
    "es": {
        "inbox": "Tareas",
        "categories": ["Trabajo", "Compras", "Salud", "Personal"],
        "tasks": [
            ("Terminar el informe de presupuesto del Q3",
             "Recopilar los números de finanzas y enviarlos a dirección antes del viernes."),
            ("Responder correos de clientes", ""),
            ("Revisar solicitudes de cambios", "Revisar las dos solicitudes abiertas del equipo móvil."),
            ("Comprar comida", "Leche, huevos, espinacas, café en grano."),
            ("Pedir el regalo de cumpleaños de mamá", ""),
            ("Recoger la tintorería", ""),
            ("Pedir cita con el dentista", "Revisión semestral, llamar antes de las 17:00."),
            ("Renovar la receta", ""),
            ("Planear la excursión del fin de semana", "Comprobar el estado del sendero y preparar la mochila el día antes."),
            ("Llamar a mamá", ""),
            ("Leer 20 páginas", ""),
            ("Regar las plantas", ""),
            ("Renovar el gimnasio", "La cuota actual termina a final de mes."),
            ("Ordenar el escritorio", ""),
        ],
        "habits": [
            "Beber agua", "Dejar de fumar", "Leer antes de dormir", "Limitar redes sociales",
            "Meditar", "Estiramiento matutino", "Escribir el diario",
        ],
        "chain_title": "Rutina matutina",
        "chain_description": "Medita, estírate y escribe en el diario antes de empezar el día.",
    },
}


def parse_args():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--today",
        type=datetime.date.fromisoformat,
        default=datetime.date.today(),
        help="Reference date to treat as 'today' (default: actual today).",
    )
    parser.add_argument("--locale", choices=sorted(LOCALES), default="en")
    parser.add_argument("--theme", choices=("light", "dark"), default="light")
    parser.add_argument("--passphrase", required=True, help="Passphrase to encrypt the export with.")
    parser.add_argument("-o", "--output", required=True, help="Output .json file path.")
    return parser.parse_args()


def d(today, offset_days):
    return (today + datetime.timedelta(days=offset_days)).isoformat()


def dt(today, offset_days, time):
    return f"{d(today, offset_days)}T{time}"


def history(today, start_offset, end_offset):
    return ",".join(d(today, o) for o in range(start_offset, end_offset + 1))


def history_with_gaps(today, offsets):
    return ",".join(d(today, o) for o in offsets)


def app_version():
    """Provenance only — the importer ignores it. Read from version.txt (the
    build's own source of truth) rather than hardcoded, so it can't drift."""
    version_file = Path(__file__).resolve().parent.parent / "version.txt"
    try:
        return version_file.read_text(encoding="utf-8").strip()
    except OSError:
        return ""


def build_plaintext(today, locale, theme):
    content = LOCALES[locale]
    work, shopping, health, personal = content["categories"]
    task_text = content["tasks"]
    habit_titles = content["habits"]

    categories = [
        # Mirrors the default category TempoDatabase.inboxCallback seeds on a fresh
        # install. It has to be in the file: Replace-mode import validates referential
        # integrity against the payload alone, and task 12 below references id -1.
        #
        # Because the file carries id -1, BackupRepositoryImpl.ensureDefaultCategory
        # leaves this row alone rather than inserting its own — so the name here is
        # what ends up on screen, and a hardcoded "Inbox" would put an English chip in
        # the Spanish screenshots. Keep LOCALES[...]["inbox"] in sync with
        # R.string.category_inbox in the matching values-<locale>/strings.xml.
        {"id": -1, "name": content["inbox"], "color": None, "icon": "inbox",
         "isDefault": True, "sortOrder": -1},
        # Personal leads the user-made categories so its chip is on screen without
        # scrolling even on a phone, where the row only fits about three. It is the
        # category capture-screenshot-set.py opens for the Tasks shot — see the note
        # on NAV there for why it has to be that one rather than Work.
        {"id": 4, "name": personal, "color": "color_m3_green", "icon": "home", "isDefault": False, "sortOrder": 0},
        {"id": 1, "name": work, "color": "color_m3_blue", "icon": "work", "isDefault": False, "sortOrder": 1},
        {"id": 2, "name": shopping, "color": "color_m3_orange", "icon": "shopping_cart", "isDefault": False, "sortOrder": 2},
        {"id": 3, "name": health, "color": "color_m3_red", "icon": "health", "isDefault": False, "sortOrder": 3},
    ]

    # (id, title, desc, isCompleted, categoryId, priority, reminderDate, periodicity,
    #  repeatDays, monthDayOption, sortOrder, completedAt)
    task_rows = [
        (1, *task_text[0], False, 1, "HIGH", dt(today, 0, "09:00"), None, None, None, 0, None),
        (2, *task_text[1], False, 1, "MEDIUM", None, None, None, None, 1, None),
        (3, *task_text[2], True, 1, None, None, None, None, None, 2, dt(today, -1, "17:30")),
        (4, *task_text[3], False, 2, "LOW", None, None, None, None, 3, None),
        (5, *task_text[4], False, 2, "MEDIUM", dt(today, 2, "12:00"), None, None, None, 4, None),
        (6, *task_text[5], True, 2, None, None, None, None, None, 5, dt(today, -2, "18:00")),
        (7, *task_text[6], False, 3, "MEDIUM", None, None, None, None, 6, None),
        (8, *task_text[7], True, 3, None, None, None, None, None, 7, dt(today, -3, "10:00")),
        (9, *task_text[8], False, 4, None, None, None, None, None, 8, None),
        (10, *task_text[9], False, 4, "HIGH", dt(today, 0, "19:00"), None, None, None, 9, None),
        (11, *task_text[10], False, 4, None, None, "DAILY", None, None, 10, None),
        (12, *task_text[11], False, -1, None, None, "WEEKLY", [1, 4], None, 11, None),
        # Two more in Personal, undated so they stay out of Focus's day and only
        # add to its tasks-without-a-date count. They are there to give the Tasks
        # screenshot — which opens Personal — a list that fills the screen.
        (13, *task_text[12], False, 4, "LOW", None, None, None, None, 12, None),
        (14, *task_text[13], False, 4, None, None, None, None, None, 13, None),
    ]
    tasks = [
        {
            "id": tid, "title": title, "description": desc, "isCompleted": completed,
            "categoryId": cat_id, "priority": priority, "reminderDate": reminder,
            "periodicity": periodicity, "periodicityInterval": 1, "repeatDays": repeat_days,
            "monthDayOption": month_day, "parentTaskId": None, "sortOrder": sort_order,
            "completedAt": completed_at, "nextInstanceId": None,
        }
        for (tid, title, desc, completed, cat_id, priority, reminder, periodicity,
             repeat_days, month_day, sort_order, completed_at) in task_rows
    ]

    # (id, title, icon, colorKey, isCompleted, habitType, createdOffset, completionHistory)
    habit_rows = [
        (1, habit_titles[0], "water", "color_m3_cyan", True, "BUILD", -60, history(today, -5, 0)),
        (2, habit_titles[1], "smoke_free", "color_m3_red", True, "QUIT", -60, history(today, -20, 0)),
        (3, habit_titles[2], "book", "color_m3_orange", True, "BUILD", -60,
         history_with_gaps(today, [-7, -6, -4, -3, -2, 0])),
        (4, habit_titles[3], "psychology", "color_m3_purple", False, "QUIT", -30, history(today, -8, -1)),
        (5, habit_titles[4], "spa", "color_m3_purple", True, "BUILD", -60, history(today, -4, 0)),
        (6, habit_titles[5], "fitness", "color_m3_blue", True, "BUILD", -60, history(today, -4, 0)),
        (7, habit_titles[6], "edit_note", "color_tempo_green", True, "BUILD", -60, history(today, -4, 0)),
    ]
    habits = [
        {
            "id": hid, "title": title, "description": "", "icon": icon, "colorKey": color_key,
            "reminderDate": None, "isCompleted": completed, "habitType": habit_type,
            "createdDate": dt(today, created_offset, "00:00"), "completionHistory": completion,
            "repeatDays": None,
        }
        for (hid, title, icon, color_key, completed, habit_type, created_offset, completion) in habit_rows
    ]

    habit_chains = [
        {
            "id": 1, "title": content["chain_title"], "description": content["chain_description"],
            "colorKey": "color_m3_purple", "icon": "spa",
            "periodicReminder": dt(today, 1, "07:00"),
            "createdDate": dt(today, -60, "00:00"),
            "completionHistory": history(today, -4, 0), "repeatDays": None,
        },
    ]
    habit_chain_members = [
        {"chainId": 1, "habitId": 5, "sortOrder": 0},
        {"chainId": 1, "habitId": 6, "sortOrder": 1},
        {"chainId": 1, "habitId": 7, "sortOrder": 2},
    ]

    return {
        "schemaVersion": SCHEMA_VERSION,
        "appVersion": app_version(),
        "exportedAt": dt(today, 0, "00:00"),
        "categories": categories,
        "tasks": tasks,
        "habits": habits,
        "habitChains": habit_chains,
        "habitChainMembers": habit_chain_members,
        # Replace-mode imports apply this section, which would otherwise clobber the
        # theme seed-screenshot-data.sh writes into theme_prefs.xml before launch.
        # Carrying the same values here keeps both paths agreeing on the theme.
        "settings": {
            "themeMode": theme.upper(),
            "useTempoColors": True,
            "routinesTabEnabled": True,
            "tasksTabEnabled": True,
            "defaultTab": "ROUTINES",
            "autoRemoveCompletedTasks": False,
            "completedTaskRetentionDays": 30,
        },
    }


def encrypt(plaintext_json: str, passphrase: str) -> dict:
    salt = os.urandom(SALT_LENGTH_BYTES)
    kdf = PBKDF2HMAC(algorithm=hashes.SHA256(), length=KEY_LENGTH_BYTES, salt=salt, iterations=ITERATIONS)
    key = kdf.derive(passphrase.encode("utf-8"))
    iv = os.urandom(GCM_IV_LENGTH_BYTES)
    ciphertext = AESGCM(key).encrypt(iv, plaintext_json.encode("utf-8"), None)
    return {
        "encryptionVersion": ENCRYPTION_VERSION,
        "kdf": KDF_NAME,
        "iterations": ITERATIONS,
        "salt": base64.b64encode(salt).decode("ascii"),
        "iv": base64.b64encode(iv).decode("ascii"),
        "ciphertext": base64.b64encode(ciphertext).decode("ascii"),
    }


def main():
    args = parse_args()
    plaintext = build_plaintext(args.today, args.locale, args.theme)
    envelope = encrypt(json.dumps(plaintext, ensure_ascii=False), args.passphrase)
    with open(args.output, "w", encoding="utf-8") as f:
        json.dump(envelope, f, ensure_ascii=False)
    print(f"Wrote encrypted backup ({args.locale}, {args.theme}) to {args.output}")


if __name__ == "__main__":
    main()
