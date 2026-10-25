# VolunteerHub

One place for high schoolers (14-18) to find, plan and prove local volunteering, instead of juggling a handful of sites.

- `android/` – the Kotlin + Jetpack Compose app. Design notes: [`design-system/MASTER.md`](design-system/MASTER.md).
- `backend/` – Python pipeline that scrapes sources, filters for teen eligibility, enriches events and writes them to Supabase.

## Run the Android app
1. Open `android/` in Android Studio (or use `./gradlew assembleDebug`).
2. To load real opportunities, add your Supabase project to `android/local.properties` (git-ignored):
   ```
   supabase.url=https://<project>.supabase.co
   supabase.anon.key=<anon public key>
   ```
   (or set `SUPABASE_URL` / `SUPABASE_ANON_KEY` environment variables).
   Without them the Discover tab shows an error state with an *Explore with sample events* option.
3. Tests: `./gradlew test`.

## Run the backend
```
pip install -r backend/requirements.txt && playwright install chromium
cp backend/.env.example backend/.env   # fill in SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY
python backend/pipeline_cron.py
cd backend && python -m unittest tests_enrich
```
Apply the SQL files in `backend/schema.sql` and `backend/migrations/` (in order, **including `004_event_content.sql`**) to your Supabase project first.
