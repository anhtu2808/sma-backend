CREATE INDEX IF NOT EXISTS idx_usage_events_subscription_feature
    ON usage_events (subscription_id, feature_id);

CREATE INDEX IF NOT EXISTS idx_usage_events_subscription_feature_created_at
    ON usage_events (subscription_id, feature_id, created_at);

CREATE INDEX IF NOT EXISTS idx_subscriptions_candidate_active_window
    ON subscriptions (candidate_id, status, start_date, end_date, purchased_at, id);

CREATE INDEX IF NOT EXISTS idx_subscriptions_company_active_window
    ON subscriptions (company_id, status, start_date, end_date, purchased_at, id);
