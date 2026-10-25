import unittest
from datetime import date, datetime, timezone

from nlp.enrich import estimate_duration_hours, parse_text_date, platform_from_url, resolve_event_date

TODAY = date(2026, 10, 25)
NOW = datetime(2026, 10, 25, 9, 0, tzinfo=timezone.utc)


class EnrichTests(unittest.TestCase):
    def test_parse_text_date(self):
        self.assertEqual(parse_text_date("Join us Nov 8 at the park", TODAY), date(2026, 11, 8))
        self.assertEqual(parse_text_date("October 30th, 2026", TODAY), date(2026, 10, 30))
        # yearless date that already passed rolls to next year
        self.assertEqual(parse_text_date("Held on Mar 3", TODAY), date(2027, 3, 3))
        # explicit past year is ignored
        self.assertIsNone(parse_text_date("Founded Jan 5, 2019", TODAY))
        self.assertIsNone(parse_text_date("no date here", TODAY))
        self.assertIsNone(parse_text_date("Feb 31", TODAY))

    def test_resolve_prefers_explicit_then_text_then_estimate(self):
        iso, est = resolve_event_date({"event_date": "2026-11-02"}, "", 0, NOW)
        self.assertTrue(iso.startswith("2026-11-02"))
        self.assertFalse(est)
        iso, est = resolve_event_date({}, "Come on Dec 6", 0, NOW)
        self.assertTrue(iso.startswith("2026-12-06"))
        self.assertFalse(est)
        iso, est = resolve_event_date({}, "no date", 4, NOW)
        self.assertTrue(est)
        self.assertTrue(iso.startswith("2026-11-0"))  # 25 Oct + 11 days
        # a past explicit date falls back instead of listing a dead event
        _, est = resolve_event_date({"event_date": "2020-01-01"}, "", 0, NOW)
        self.assertTrue(est)

    def test_duration(self):
        self.assertEqual(estimate_duration_hours("a 4 hour shift"), 4.0)
        self.assertEqual(estimate_duration_hours("about 2.5 hrs"), 2.5)
        self.assertEqual(estimate_duration_hours("no mention"), 3.0)
        self.assertEqual(estimate_duration_hours("40 hours of training"), 3.0)

    def test_platform(self):
        self.assertEqual(platform_from_url("https://www.idealist.org/en/x"), "Idealist")
        self.assertEqual(platform_from_url("https://mindstretchingfun.org/volunteer/"), "mindstretchingfun.org")
        self.assertEqual(platform_from_url(""), "")


if __name__ == "__main__":
    unittest.main()
