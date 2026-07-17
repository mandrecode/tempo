package com.mandrecode.tempo.core.ui.theme

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class TempoIconTest {
    private lateinit var context: Context

    private val keywordMap =
        mapOf(
            R.string.keywords_fitness to "fitness, gym, workout, exercise, training, strength",
            R.string.keywords_run to "run, running, jog, jogging, cardio, sprint",
            R.string.keywords_walk to "walk, walking, step, steps, stroll, hike",
            R.string.keywords_sports to "sport, sports, game, play, ball, team",
            R.string.keywords_health to "health, medical, doctor, checkup, medicine",
            R.string.keywords_heart to "heart, love, care, gratitude, affection",
            R.string.keywords_mood to "mood, happy, smile, emotion, feeling, mental, mindfulness",
            R.string.keywords_spa to "spa, relax, massage, calm, meditation, zen",
            R.string.keywords_restaurant to "eat, food, meal, lunch, dinner, breakfast, restaurant",
            R.string.keywords_coffee to "coffee, cafe, tea, drink, beverage",
            R.string.keywords_water to "water, hydrate, drink, hydration, liquid",
            R.string.keywords_work to "work, job, office, career, business",
            R.string.keywords_school to "school, study, learn, education, class, university",
            R.string.keywords_book to "book, read, reading, literature, novel, study",
            R.string.keywords_create to "create, write, writing, draw, art, design, creative",
            R.string.keywords_home to "home, house, clean, tidy, chore",
            R.string.keywords_calendar to "calendar, schedule, plan, organize, appointment",
            R.string.keywords_bed to "sleep, bed, rest, nap, bedtime, night",
            R.string.keywords_call to "call, phone, contact, talk, conversation",
            R.string.keywords_email to "email, mail, message, inbox, correspondence",
            R.string.keywords_music to "music, song, listen, audio, play, instrument, practice",
            R.string.keywords_music_note to "note, melody, tune, compose, musician, piano, guitar",
            R.string.keywords_game to "game, gaming, play, video game, console",
            R.string.keywords_movie to "movie, film, watch, cinema, tv, show",
            R.string.keywords_star to "goal, achieve, success, milestone, reward, favorite",
            R.string.keywords_trophy to "trophy, win, achievement, award, victory, champion",
            R.string.keywords_target to "target, focus, aim, goal, objective",
            R.string.keywords_list to "menu, list, organize, sort",
            R.string.keywords_remind to "remind, reminder, bell, alert, notification",
            R.string.keywords_routine to "routine, daily, habit, repeat, recurring, schedule, regular",
            R.string.keywords_bedtime to "wind down, moon, night routine, lights out",
            R.string.keywords_medical_services to "clinic, hospital, appointment, physician, specialist",
            R.string.keywords_no_food to "fast, fasting, no food, intermittent fasting, skip eating",
            R.string.keywords_code to "code, coding, programming, developer, software",
            R.string.keywords_translate to "language, learn language, vocabulary, translate, practice",
            R.string.keywords_savings to "save, saving, savings, piggy bank, invest",
        )

    private val spanishKeywordMap =
        mapOf(
            R.string.keywords_fitness to "fitness, gimnasio, entrenamiento, ejercicio, entrenar, fuerza",
            R.string.keywords_run to "correr, corriendo, trotar, trote, cardio, sprint",
            R.string.keywords_walk to "caminar, caminando, paso, pasos, paseo, caminata",
            R.string.keywords_sports to "deporte, deportes, juego, jugar, pelota, equipo",
            R.string.keywords_health to "salud, médico, doctor, chequeo, medicina",
            R.string.keywords_heart to "corazón, amor, cuidado, gratitud, cariño",
            R.string.keywords_mood to "humor, feliz, sonrisa, emoción, sentimiento, mental, mindfulness",
            R.string.keywords_spa to "spa, relax, masaje, calma, meditación, zen",
            R.string.keywords_restaurant to "comer, comida, almuerzo, cena, desayuno, restaurante",
            R.string.keywords_coffee to "café, cafetería, té, beber, bebida",
            R.string.keywords_water to "agua, hidratar, beber, hidratación, líquido",
            R.string.keywords_work to "trabajo, empleo, oficina, carrera, negocio",
            R.string.keywords_school to "escuela, estudiar, aprender, educación, clase, universidad",
            R.string.keywords_book to "libro, leer, leyendo, lectura, literatura, novela, estudio",
            R.string.keywords_create to "crear, escribir, escribiendo, dibujar, arte, diseño, creativo",
            R.string.keywords_home to "casa, hogar, limpiar, ordenar, tarea",
            R.string.keywords_calendar to "calendario, horario, plan, organizar, cita",
            R.string.keywords_bed to "dormir, cama, descanso, siesta, hora de dormir, noche",
            R.string.keywords_call to "llamar, teléfono, contacto, hablar, conversación",
            R.string.keywords_email to "email, correo, mensaje, bandeja de entrada, correspondencia",
            R.string.keywords_music to "música, canción, escuchar, audio, reproducir, instrumento, práctica",
            R.string.keywords_music_note to "nota, melodía, melodia, afinar, componer, músico, piano, guitarra",
            R.string.keywords_game to "juego, gaming, jugar, videojuego, consola",
            R.string.keywords_movie to "película, cine, ver, tv, programa",
            R.string.keywords_star to "meta, lograr, éxito, hito, recompensa, favorito",
            R.string.keywords_trophy to "trofeo, ganar, logro, premio, victoria, campeón",
            R.string.keywords_target to "objetivo, enfoque, meta, propósito",
            R.string.keywords_list to "menú, lista, organizar, ordenar",
            R.string.keywords_remind to "recordar, recordatorio, campana, alerta, notificación",
            R.string.keywords_routine to "rutina, diario, hábito, repetir, recurrente, horario, regular",
            R.string.keywords_bedtime to "rutina nocturna, luna, apagar luces",
            R.string.keywords_medical_services to "clínica, hospital, cita médica, médico, especialista",
            R.string.keywords_no_food to "ayuno, ayunar, ayuno intermitente, día de ayuno",
            R.string.keywords_code to "código, programar, programación, desarrollador, software",
            R.string.keywords_translate to "idioma, aprender idioma, vocabulario, traducir, practicar",
            R.string.keywords_savings to "ahorrar, ahorro, alcancía, invertir",
        )

    @Before
    fun setup() {
        context = mockk()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun setupKeywords(isSpanish: Boolean = false) {
        every { context.getString(any()) } answers {
            val resId = it.invocation.args[0] as Int
            if (isSpanish && spanishKeywordMap.containsKey(resId)) {
                spanishKeywordMap[resId]!!
            } else {
                keywordMap[resId] ?: ""
            }
        }
    }

    @Test
    fun `fromName returns correct icon when name exists`() {
        val icon = TempoIcon.fromName("run")

        assertThat(icon).isNotNull()
        assertThat(icon).isEqualTo(TempoIcon.RUN)
        assertThat(icon?.iconName).isEqualTo("run")
    }

    @Test
    fun `fromName returns null when name does not exist`() {
        val icon = TempoIcon.fromName("nonexistent")
        assertThat(icon).isNull()
    }

    @Test
    fun `fromName returns null when name is null`() {
        val icon = TempoIcon.fromName(null)
        assertThat(icon).isNull()
    }

    @Test
    fun `getAllIcons returns all icons in enum`() {
        val icons = TempoIcon.getAllIcons()

        assertThat(icons).isNotNull()
        assertThat(icons).hasSize(TempoIcon.entries.size)
    }

    // --- ENGLISH TESTS ---

    @Test
    fun `suggestIcon returns correct icon for fitness keywords`() {
        setupKeywords()
        assertThat(TempoIcon.suggestIcon("Morning gym workout", context)).isEqualTo(TempoIcon.FITNESS)
        assertThat(TempoIcon.suggestIcon("Exercise daily", context)).isEqualTo(TempoIcon.FITNESS)
        assertThat(TempoIcon.suggestIcon("training session", context)).isEqualTo(TempoIcon.FITNESS)
    }

    @Test
    fun `suggestIcon returns correct icon for running keywords`() {
        setupKeywords()
        assertThat(TempoIcon.suggestIcon("Morning run", context)).isEqualTo(TempoIcon.RUN)
        assertThat(TempoIcon.suggestIcon("Go jogging", context)).isEqualTo(TempoIcon.RUN)
        assertThat(TempoIcon.suggestIcon("cardio sprint", context)).isEqualTo(TempoIcon.RUN)
    }

    @Test
    fun `suggestIcon returns correct icon for walking keywords`() {
        setupKeywords()
        assertThat(TempoIcon.suggestIcon("Morning walk", context)).isEqualTo(TempoIcon.WALK)
        assertThat(TempoIcon.suggestIcon("Take steps", context)).isEqualTo(TempoIcon.WALK)
        assertThat(TempoIcon.suggestIcon("walking daily", context)).isEqualTo(TempoIcon.WALK)
    }

    @Test
    fun `suggestIcon returns correct icon for health keywords`() {
        setupKeywords()
        assertThat(TempoIcon.suggestIcon("Doctor checkup", context)).isEqualTo(TempoIcon.HEALTH)
        assertThat(TempoIcon.suggestIcon("Take medicine", context)).isEqualTo(TempoIcon.HEALTH)
        assertThat(TempoIcon.suggestIcon("Medical appointment", context)).isEqualTo(TempoIcon.HEALTH)
    }

    @Test
    fun `suggestIcon returns correct icon for water keywords`() {
        setupKeywords()
        // "water" matches WATER's own "water" + "drink" keywords (2 hits) which now
        // outscores COFFEE's single "drink" hit
        assertThat(TempoIcon.suggestIcon("Drink water", context))
            .isEqualTo(TempoIcon.WATER)
        assertThat(TempoIcon.suggestIcon("Stay hydrated", context)).isEqualTo(TempoIcon.WATER)
        assertThat(TempoIcon.suggestIcon("hydration reminder", context)).isEqualTo(TempoIcon.WATER)
    }

    @Test
    fun `suggestIcon returns correct icon for reading keywords`() {
        setupKeywords()
        assertThat(TempoIcon.suggestIcon("Read book", context)).isEqualTo(TempoIcon.BOOK)
        assertThat(TempoIcon.suggestIcon("reading time", context)).isEqualTo(TempoIcon.BOOK)
        // BOOK's "study" + "literature" (2 hits) outscores SCHOOL's single "study" hit
        assertThat(TempoIcon.suggestIcon("Study literature", context)).isEqualTo(TempoIcon.BOOK)
    }

    @Test
    fun `suggestIcon does not match keywords inside unrelated words`() {
        setupKeywords()
        // "run" must not match inside "brunch"
        assertThat(TempoIcon.suggestIcon("Weekend brunch", context)).isNull()
    }

    @Test
    fun `suggestIcon still matches regular suffixed forms of longer keywords`() {
        setupKeywords()
        // "clean" (prefix match) + "house" (exact) both point to HOME
        assertThat(TempoIcon.suggestIcon("I'm cleaning the house", context)).isEqualTo(TempoIcon.HOME)
    }

    @Test
    fun `suggestIcon still matches plurals of short keywords`() {
        setupKeywords()
        // "meal" (4 chars, hard right boundary) must still match plural "Meals"
        assertThat(TempoIcon.suggestIcon("Meals prep", context)).isEqualTo(TempoIcon.RESTAURANT)
    }

    @Test
    fun `suggestIcon returns correct icon for meditation keywords`() {
        setupKeywords()
        assertThat(TempoIcon.suggestIcon("Meditation", context)).isEqualTo(TempoIcon.SPA)
        assertThat(TempoIcon.suggestIcon("Relax time", context)).isEqualTo(TempoIcon.SPA)
        assertThat(TempoIcon.suggestIcon("zen practice", context)).isEqualTo(TempoIcon.SPA)
    }

    @Test
    fun `suggestIcon returns correct icon for sleep keywords`() {
        setupKeywords()
        assertThat(TempoIcon.suggestIcon("Sleep early", context)).isEqualTo(TempoIcon.BED)
        assertThat(TempoIcon.suggestIcon("Bedtime routine", context)).isEqualTo(TempoIcon.BED)
        assertThat(TempoIcon.suggestIcon("Rest time", context)).isEqualTo(TempoIcon.BED)
    }

    @Test
    fun `suggestIcon returns correct icon for bedtime keywords, distinct from bed`() {
        setupKeywords()
        assertThat(TempoIcon.suggestIcon("Wind down time", context)).isEqualTo(TempoIcon.BEDTIME)
        assertThat(TempoIcon.suggestIcon("Lights out", context)).isEqualTo(TempoIcon.BEDTIME)
        // BED still wins when the text uses "bedtime" itself - BEDTIME doesn't claim that word
        assertThat(TempoIcon.suggestIcon("Bedtime story", context)).isEqualTo(TempoIcon.BED)
    }

    @Test
    fun `suggestIcon returns correct icon for medical services keywords, distinct from health`() {
        setupKeywords()
        assertThat(TempoIcon.suggestIcon("Hospital appointment", context)).isEqualTo(TempoIcon.MEDICAL_SERVICES)
        assertThat(TempoIcon.suggestIcon("See a specialist", context)).isEqualTo(TempoIcon.MEDICAL_SERVICES)
        // HEALTH still wins on its own distinct "medical"/"medicine" keywords
        assertThat(TempoIcon.suggestIcon("Doctor checkup", context)).isEqualTo(TempoIcon.HEALTH)
    }

    @Test
    fun `suggestIcon returns correct icon for fasting keywords`() {
        setupKeywords()
        assertThat(TempoIcon.suggestIcon("Intermittent fasting", context)).isEqualTo(TempoIcon.NO_FOOD)
        assertThat(TempoIcon.suggestIcon("Skip eating today", context)).isEqualTo(TempoIcon.NO_FOOD)
    }

    @Test
    fun `suggestIcon returns correct icon for coding keywords`() {
        setupKeywords()
        assertThat(TempoIcon.suggestIcon("Practice coding", context)).isEqualTo(TempoIcon.CODE)
        assertThat(TempoIcon.suggestIcon("Programming session", context)).isEqualTo(TempoIcon.CODE)
    }

    @Test
    fun `suggestIcon returns correct icon for language learning keywords`() {
        setupKeywords()
        assertThat(TempoIcon.suggestIcon("Learn language vocabulary", context)).isEqualTo(TempoIcon.TRANSLATE)
    }

    @Test
    fun `suggestIcon returns correct icon for savings keywords`() {
        setupKeywords()
        assertThat(TempoIcon.suggestIcon("Piggy bank savings", context)).isEqualTo(TempoIcon.SAVINGS)
        assertThat(TempoIcon.suggestIcon("Invest monthly", context)).isEqualTo(TempoIcon.SAVINGS)
    }

    @Test
    fun `suggestIcon returns correct icon for music note keywords`() {
        setupKeywords()
        assertThat(TempoIcon.suggestIcon("guitar tune", context)).isEqualTo(TempoIcon.MUSIC_NOTE)
        assertThat(TempoIcon.suggestIcon("Piano lesson", context)).isEqualTo(TempoIcon.MUSIC_NOTE)
        assertThat(TempoIcon.suggestIcon("compose a melody", context)).isEqualTo(TempoIcon.MUSIC_NOTE)
    }

    @Test
    fun `suggestIcon is case insensitive`() {
        setupKeywords()
        assertThat(TempoIcon.suggestIcon("MORNING RUN", context)).isEqualTo(TempoIcon.RUN)
        assertThat(TempoIcon.suggestIcon("morning run", context)).isEqualTo(TempoIcon.RUN)
        assertThat(TempoIcon.suggestIcon("MoRnInG rUn", context)).isEqualTo(TempoIcon.RUN)
    }

    @Test
    fun `suggestIcon matches partial keywords`() {
        setupKeywords()
        assertThat(TempoIcon.suggestIcon("walking", context)).isEqualTo(TempoIcon.WALK)
        assertThat(TempoIcon.suggestIcon("running", context)).isEqualTo(TempoIcon.RUN)
    }

    // --- SPANISH TESTS ---

    @Test
    fun `suggestIcon returns correct icon for fitness keywords Spanish`() {
        setupKeywords(isSpanish = true)
        assertThat(TempoIcon.suggestIcon("Ir al gimnasio", context)).isEqualTo(TempoIcon.FITNESS)
        assertThat(TempoIcon.suggestIcon("Hacer ejercicio", context)).isEqualTo(TempoIcon.FITNESS)
        assertThat(TempoIcon.suggestIcon("Sesión de entrenamiento", context)).isEqualTo(TempoIcon.FITNESS)
    }

    @Test
    fun `suggestIcon returns correct icon for running keywords Spanish`() {
        setupKeywords(isSpanish = true)
        assertThat(TempoIcon.suggestIcon("Salir a correr", context)).isEqualTo(TempoIcon.RUN)
        assertThat(TempoIcon.suggestIcon("Trote matutino", context)).isEqualTo(TempoIcon.RUN)
        assertThat(TempoIcon.suggestIcon("Hacer cardio", context)).isEqualTo(TempoIcon.RUN)
    }

    @Test
    fun `suggestIcon returns correct icon for walking keywords Spanish`() {
        setupKeywords(isSpanish = true)
        assertThat(TempoIcon.suggestIcon("Dar un paseo", context)).isEqualTo(TempoIcon.WALK)
        assertThat(TempoIcon.suggestIcon("Caminar por el parque", context)).isEqualTo(TempoIcon.WALK)
        assertThat(TempoIcon.suggestIcon("Caminata diaria", context)).isEqualTo(TempoIcon.WALK)
    }

    @Test
    fun `suggestIcon returns correct icon for health keywords Spanish`() {
        setupKeywords(isSpanish = true)
        assertThat(TempoIcon.suggestIcon("Salud", context)).isEqualTo(TempoIcon.HEALTH)
        assertThat(TempoIcon.suggestIcon("Tomar medicina", context)).isEqualTo(TempoIcon.HEALTH)
        assertThat(TempoIcon.suggestIcon("Chequeo doctor", context)).isEqualTo(TempoIcon.HEALTH)
    }

    @Test
    fun `suggestIcon returns correct icon for water keywords Spanish`() {
        setupKeywords(isSpanish = true)
        // "agua" + "beber" (2 hits) outscores COFFEE's single "beber" hit
        assertThat(TempoIcon.suggestIcon("Beber agua", context)).isEqualTo(TempoIcon.WATER)
        assertThat(TempoIcon.suggestIcon("Hidratar el cuerpo", context)).isEqualTo(TempoIcon.WATER)
        assertThat(TempoIcon.suggestIcon("Recordatorio hidratación", context)).isEqualTo(TempoIcon.WATER)
    }

    @Test
    fun `suggestIcon does not match keywords inside unrelated Spanish words`() {
        setupKeywords(isSpanish = true)
        // "ver" (movie/watch) must not match inside "verano" (summer)
        assertThat(TempoIcon.suggestIcon("Viaje de verano", context)).isNull()
    }

    @Test
    fun `suggestIcon still matches regular suffixed forms of longer Spanish keywords`() {
        setupKeywords(isSpanish = true)
        // "empleos" (plural) matches WORK's "empleo" via prefix leniency
        assertThat(TempoIcon.suggestIcon("Buscar empleos nuevos", context)).isEqualTo(TempoIcon.WORK)
    }

    @Test
    fun `suggestIcon returns correct icon for reading keywords Spanish`() {
        setupKeywords(isSpanish = true)
        assertThat(TempoIcon.suggestIcon("libro", context)).isEqualTo(TempoIcon.BOOK)
        assertThat(TempoIcon.suggestIcon("Tiempo de lectura", context)).isEqualTo(TempoIcon.BOOK)
        assertThat(TempoIcon.suggestIcon("Estudiar examen", context)).isEqualTo(TempoIcon.SCHOOL)
    }

    @Test
    fun `suggestIcon returns correct icon for meditation keywords Spanish`() {
        setupKeywords(isSpanish = true)
        assertThat(TempoIcon.suggestIcon("Meditación zen", context)).isEqualTo(TempoIcon.SPA)
        assertThat(TempoIcon.suggestIcon("Tiempo de relax", context)).isEqualTo(TempoIcon.SPA)
        assertThat(TempoIcon.suggestIcon("Masaje calma", context)).isEqualTo(TempoIcon.SPA)
    }

    @Test
    fun `suggestIcon returns correct icon for sleep keywords Spanish`() {
        setupKeywords(isSpanish = true)
        assertThat(TempoIcon.suggestIcon("Dormir temprano", context)).isEqualTo(TempoIcon.BED)
        assertThat(TempoIcon.suggestIcon("Descanso nocturno", context)).isEqualTo(TempoIcon.BED)
        assertThat(TempoIcon.suggestIcon("Hora de dormir", context)).isEqualTo(TempoIcon.BED)
    }

    @Test
    fun `suggestIcon returns correct icon for bedtime keywords Spanish, distinct from bed`() {
        setupKeywords(isSpanish = true)
        assertThat(TempoIcon.suggestIcon("Rutina nocturna", context)).isEqualTo(TempoIcon.BEDTIME)
        assertThat(TempoIcon.suggestIcon("Apagar luces", context)).isEqualTo(TempoIcon.BEDTIME)
        assertThat(TempoIcon.suggestIcon("Dormir temprano", context)).isEqualTo(TempoIcon.BED)
    }

    @Test
    fun `suggestIcon returns correct icon for fasting keywords Spanish`() {
        setupKeywords(isSpanish = true)
        assertThat(TempoIcon.suggestIcon("Ayuno intermitente", context)).isEqualTo(TempoIcon.NO_FOOD)
    }

    @Test
    fun `suggestIcon returns correct icon for coding keywords Spanish`() {
        setupKeywords(isSpanish = true)
        assertThat(TempoIcon.suggestIcon("Practicar programación", context)).isEqualTo(TempoIcon.CODE)
    }

    @Test
    fun `suggestIcon returns correct icon for savings keywords Spanish`() {
        setupKeywords(isSpanish = true)
        assertThat(TempoIcon.suggestIcon("Ahorrar en la alcancía", context)).isEqualTo(TempoIcon.SAVINGS)
    }

    @Test
    fun `suggestIcon returns correct icon for music note keywords Spanish`() {
        setupKeywords(isSpanish = true)
        assertThat(TempoIcon.suggestIcon("Nota musical", context)).isEqualTo(TempoIcon.MUSIC_NOTE)
        assertThat(TempoIcon.suggestIcon("Tocar piano", context)).isEqualTo(TempoIcon.MUSIC_NOTE)
        assertThat(TempoIcon.suggestIcon("Afinar guitarra", context)).isEqualTo(TempoIcon.MUSIC_NOTE)
    }

    @Test
    fun `suggestIcon is case insensitive Spanish`() {
        setupKeywords(isSpanish = true)
        assertThat(TempoIcon.suggestIcon("SALIR A CORRER", context)).isEqualTo(TempoIcon.RUN)
        assertThat(TempoIcon.suggestIcon("salir a correr", context)).isEqualTo(TempoIcon.RUN)
        assertThat(TempoIcon.suggestIcon("SaLiR a CoRrEr", context)).isEqualTo(TempoIcon.RUN)
    }

    @Test
    fun `suggestIcon matches partial keywords Spanish`() {
        setupKeywords(isSpanish = true)
        assertThat(TempoIcon.suggestIcon("caminando", context)).isEqualTo(TempoIcon.WALK)
        assertThat(TempoIcon.suggestIcon("corriendo", context)).isEqualTo(TempoIcon.RUN)
    }

    // --- GENERIC TESTS ---

    @Test
    fun `all icons have unique names`() {
        val icons = TempoIcon.getAllIcons()
        val names = icons.map { it.iconName }
        val uniqueNames = names.toSet()

        assertThat(uniqueNames).hasSize(names.size)
    }

    @Test
    fun `all icons have valid drawable resources`() {
        val icons = TempoIcon.getAllIcons()

        icons.forEach { icon ->
            assertThat(icon.iconRes).isGreaterThan(0)
        }
    }

    @Test
    fun `every icon category has at least one icon`() {
        val icons = TempoIcon.getAllIcons()
        val categoriesInUse = icons.map { it.category }.toSet()

        assertThat(categoriesInUse).containsExactlyElementsIn(IconCategory.entries)
    }

    // --- sampleAcrossCategories TESTS ---

    @Test
    fun `sampleAcrossCategories fills distinct categories when slots fit`() {
        val icons = TempoIcon.getAllIcons()
        val slotCount = 5

        val sampled = TempoIcon.sampleAcrossCategories(icons, slotCount, random = Random(42))

        assertThat(sampled).hasSize(slotCount)
        assertThat(sampled.map { it.category }.toSet()).hasSize(slotCount)
        assertThat(sampled.toSet()).hasSize(slotCount)
    }

    @Test
    fun `sampleAcrossCategories never returns duplicate icons when wrapping`() {
        val icons = TempoIcon.getAllIcons()
        val slotCount = icons.size + 10

        val sampled = TempoIcon.sampleAcrossCategories(icons, slotCount, random = Random(7))

        assertThat(sampled).hasSize(icons.size)
        assertThat(sampled.toSet()).hasSize(icons.size)
    }

    @Test
    fun `sampleAcrossCategories is deterministic for a given seed`() {
        val icons = TempoIcon.getAllIcons()

        val first = TempoIcon.sampleAcrossCategories(icons, 6, random = Random(99))
        val second = TempoIcon.sampleAcrossCategories(icons, 6, random = Random(99))

        assertThat(first).isEqualTo(second)
    }

    @Test
    fun `sampleAcrossCategories returns empty list for non-positive slot count`() {
        val icons = TempoIcon.getAllIcons()

        assertThat(TempoIcon.sampleAcrossCategories(icons, 0)).isEmpty()
        assertThat(TempoIcon.sampleAcrossCategories(icons, -1)).isEmpty()
    }
}
