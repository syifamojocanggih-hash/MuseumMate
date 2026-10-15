# MuseumMate Development Guidelines

MuseumMate adalah aplikasi desktop berbasis Java (Java 21 LTS + JavaFX) untuk manajemen koleksi dan konservasi museum dengan perhitungan prioritas perawatan otomatis.

## Arsitektur Proyek
- `model`: Class domain (Koleksi, Lukisan, Artefak, Naskah, Fosil, Pengguna, Pemeriksaan, JadwalPerawatan, Enums).
- `dao`: Lapisan JDBC Data Access Object.
- `service`: Logika bisnis (Prioritas, Auth, Validasi, Pelaporan PDF).
- `controller`: Controller JavaFX (Event handling & binding).
- `util`: Helper koneksi basis data, BCrypt, session, formatting.
- `view`: FXML file antarmuka.

<!-- antislop:start -->
## antislop
For UI, copy, people, mobile layout, or code comments work, read `antislop.md` (core) and then the skill for the task:
- UI / visual: `skills/antislop-ui/SKILL.md`
- Copy & text: `skills/antislop-copywriting/SKILL.md`
- People: `skills/antislop-human/SKILL.md`
- Mobile / responsive: `skills/antislop-layoutmobile/SKILL.md`
- Code comments: `skills/antislop-code/SKILL.md`
Before starting, follow the core's "Two Usage Modes" section in strict order: explicit session instruction first, then global preference, then ask. A session instruction always wins. For a resolved mode, say `antislop active: <mode> (session override).` or `antislop active: <mode> (global preference).` once before presenting findings or making edits, using the actual mode and source. Acknowledging the user's request without naming the source does not replace this notice.
Only an explicit choice of antislop during or after selects a session mode. A request to review, audit, or avoid file edits does not select a mode; read the global preference in that case. Another skill's mode does not select antislop's mode.
If the mode is unresolved, ask during/after and end the response; wait for the answer before any UI review, planning, or concept. For read-only tasks, put the active-mode notice only at the start of the final answer, never in progress messages. For editing tasks, announce before the first edit and omit it from the final answer.
To update antislop later: download `antislop.md` again, or run `npx antislop-ai --update` if it was installed as skill folders.
<!-- antislop:end -->
