"""Small, dependency-free helpers that turn scraped text into the structured fields the app shows."""
import re
from datetime import date, datetime, timedelta, timezone
from urllib.parse import urlparse

_MONTHS = {m: i + 1 for i, m in enumerate(
    ["jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"])}
_DATE_RE = re.compile(
    r"\b(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\.?\s+(\d{1,2})(?:st|nd|rd|th)?(?:,?\s*(\d{4}))?\b",
    re.IGNORECASE,
)
_HOURS_RE = re.compile(r"\b(\d{1,2}(?:\.\d)?)\s*(?:-|to)?\s*(?:\d{1,2}(?:\.\d)?\s*)?(?:hours?|hrs?)\b", re.IGNORECASE)

_PLATFORMS = {
    "idealist.org": "Idealist",
    "volunteermatch.org": "VolunteerMatch",
    "ntfb.org": "North Texas Food Bank",
    "friscotexas.gov": "City of Frisco",
    "friscolibrary.com": "Frisco Public Library",
    "mckinneytexas.org": "City of McKinney",
}


def parse_text_date(text, today=None):
    """Return the first 'Oct 25' / 'October 25, 2026' style date in `text` as a `date` that is not in the past.

    A date without a year is assumed to be the next occurrence. Returns None when nothing parses.
    """
    today = today or date.today()
    for match in _DATE_RE.finditer(text or ""):
        month = _MONTHS[match.group(1).lower()[:3]]
        day = int(match.group(2))
        year = int(match.group(3)) if match.group(3) else today.year
        try:
            found = date(year, month, day)
        except ValueError:
            continue
        if found < today and not match.group(3):
            try:
                found = date(year + 1, month, day)
            except ValueError:
                continue
        if found >= today:
            return found
    return None


def resolve_event_date(event, text, index, now=None):
    """Pick the best date for an event and say whether it is a placeholder.

    Order: an explicit `event_date` on the raw record, then a date found in the text, then an
    estimated date staggered over the coming weeks. Returns (iso_datetime_string, is_estimated).
    """
    now = now or datetime.now(timezone.utc)
    explicit = event.get("event_date")
    if explicit:
        try:
            parsed = datetime.fromisoformat(str(explicit).replace("Z", "+00:00"))
            if parsed.tzinfo is None:
                parsed = parsed.replace(tzinfo=timezone.utc)
            if parsed.date() >= now.date():
                return parsed.isoformat(), False
        except ValueError:
            pass
    found = parse_text_date(text, now.date())
    if found:
        return datetime(found.year, found.month, found.day, 12, 0, tzinfo=timezone.utc).isoformat(), False
    estimated = now + timedelta(days=(index % 14) * 2 + 3)
    return estimated.isoformat(), True


def estimate_duration_hours(text, default=3.0):
    """Look for '4 hours' / '2.5 hrs' in the text; clamp to a sensible shift length."""
    match = _HOURS_RE.search(text or "")
    if not match:
        return default
    try:
        hours = float(match.group(1))
    except ValueError:
        return default
    return hours if 0.5 <= hours <= 12 else default


def platform_from_url(url):
    host = (urlparse(url or "").hostname or "").lower()
    if host.startswith("www."):
        host = host[4:]
    for domain, name in _PLATFORMS.items():
        if host == domain or host.endswith("." + domain):
            return name
    return host
