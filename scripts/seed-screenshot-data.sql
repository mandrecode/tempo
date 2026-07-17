-- Demo data for Google Play Store screenshots (GitHub issue #169).
-- Run only against a freshly created app DB (Inbox category already seeded
-- by TempoDatabase.inboxCallback). Not part of the CI/app build.

INSERT INTO categories (id, name, color, icon, isDefault, sortOrder) VALUES
  (1, 'Work', 'color_m3_blue', 'work', 0, 0),
  (2, 'Shopping', 'color_m3_orange', 'shopping_cart', 0, 1),
  (3, 'Health', 'color_m3_red', 'health', 0, 2),
  (4, 'Personal', 'color_m3_green', 'home', 0, 3);

INSERT INTO tasks (id, title, description, isCompleted, categoryId, priority, reminderDate, periodicity, periodicityInterval, repeatDays, monthDayOption, parentTaskId, sortOrder, completedAt, nextInstanceId) VALUES
  (1, 'Finish Q3 budget report', 'Compile numbers from finance and send to leadership by Friday.', 0, 1, 'HIGH', '2026-07-17T09:00', NULL, 1, NULL, NULL, NULL, 0, NULL, NULL),
  (2, 'Reply to client emails', '', 0, 1, 'MEDIUM', NULL, NULL, 1, NULL, NULL, NULL, 1, NULL, NULL),
  (3, 'Review pull requests', 'Check the two open PRs from the mobile team.', 1, 1, NULL, NULL, NULL, 1, NULL, NULL, NULL, 2, '2026-07-16T17:30', NULL),
  (4, 'Buy groceries', 'Milk, eggs, spinach, coffee beans.', 0, 2, 'LOW', NULL, NULL, 1, NULL, NULL, NULL, 3, NULL, NULL),
  (5, 'Order birthday gift for Mom', '', 0, 2, 'MEDIUM', '2026-07-19T12:00', NULL, 1, NULL, NULL, NULL, 4, NULL, NULL),
  (6, 'Pick up dry cleaning', '', 1, 2, NULL, NULL, NULL, 1, NULL, NULL, NULL, 5, '2026-07-15T18:00', NULL),
  (7, 'Book dentist appointment', 'Six-month checkup, call before 5pm.', 0, 3, 'MEDIUM', NULL, NULL, 1, NULL, NULL, NULL, 6, NULL, NULL),
  (8, 'Refill prescription', '', 1, 3, NULL, NULL, NULL, 1, NULL, NULL, NULL, 7, '2026-07-14T10:00', NULL),
  (9, 'Plan weekend hike', 'Check trail conditions and pack the day before.', 0, 4, NULL, NULL, NULL, 1, NULL, NULL, NULL, 8, NULL, NULL),
  (10, 'Call Mom', '', 0, 4, 'HIGH', '2026-07-17T19:00', NULL, 1, NULL, NULL, NULL, 9, NULL, NULL),
  (11, 'Read 20 pages', '', 0, 4, NULL, NULL, 'DAILY', 1, NULL, NULL, NULL, 10, NULL, NULL),
  (12, 'Water the plants', '', 0, -1, NULL, NULL, 'WEEKLY', 1, '1,4', NULL, NULL, 11, NULL, NULL);

INSERT INTO habits (id, title, description, icon, colorKey, reminderDate, isCompleted, habitType, createdDate, completionHistory, repeatDays) VALUES
  (1, 'Drink Water', '', 'water', 'color_m3_cyan', NULL, 1, 'BUILD', '2026-05-01T00:00', '2026-07-12,2026-07-13,2026-07-14,2026-07-15,2026-07-16,2026-07-17', NULL),
  (2, 'No Smoking', '', 'smoke_free', 'color_m3_red', NULL, 1, 'QUIT', '2026-06-01T00:00', '2026-06-27,2026-06-28,2026-06-29,2026-06-30,2026-07-01,2026-07-02,2026-07-03,2026-07-04,2026-07-05,2026-07-06,2026-07-07,2026-07-08,2026-07-09,2026-07-10,2026-07-11,2026-07-12,2026-07-13,2026-07-14,2026-07-15,2026-07-16,2026-07-17', NULL),
  (3, 'Read Before Bed', '', 'book', 'color_m3_orange', NULL, 1, 'BUILD', '2026-06-15T00:00', '2026-07-10,2026-07-11,2026-07-13,2026-07-14,2026-07-15,2026-07-17', NULL),
  (4, 'Limit Social Media', '', 'psychology', 'color_m3_purple', NULL, 0, 'QUIT', '2026-07-01T00:00', '2026-07-09,2026-07-10,2026-07-11,2026-07-12,2026-07-13,2026-07-14,2026-07-15,2026-07-16', NULL),
  (5, 'Meditate', '', 'spa', 'color_m3_purple', NULL, 1, 'BUILD', '2026-06-01T00:00', '2026-07-13,2026-07-14,2026-07-15,2026-07-16,2026-07-17', NULL),
  (6, 'Morning Stretch', '', 'fitness', 'color_m3_blue', NULL, 1, 'BUILD', '2026-06-01T00:00', '2026-07-13,2026-07-14,2026-07-15,2026-07-16,2026-07-17', NULL),
  (7, 'Journal', '', 'edit_note', 'color_tempo_green', NULL, 1, 'BUILD', '2026-06-01T00:00', '2026-07-13,2026-07-14,2026-07-15,2026-07-16,2026-07-17', NULL);

INSERT INTO habit_chains (id, title, description, colorKey, icon, periodicReminder, createdDate, completionHistory, repeatDays) VALUES
  (1, 'Morning Routine', 'Meditate, stretch, and journal before starting the day.', 'color_m3_purple', 'spa', '2026-07-18T07:00', '2026-06-01T00:00', '2026-07-13,2026-07-14,2026-07-15,2026-07-16,2026-07-17', NULL);

INSERT INTO habit_chain_members (chainId, habitId, sortOrder) VALUES
  (1, 5, 0),
  (1, 6, 1),
  (1, 7, 2);
