DROP INDEX IF EXISTS idx_usage_events_subscription_feature;

DROP INDEX IF EXISTS idx_usage_events_subscription_feature_created_at;

CREATE INDEX IF NOT EXISTS idx_usage_events_subscription_feature_status
    ON usage_events (subscription_id, feature_id, status);

CREATE INDEX IF NOT EXISTS idx_usage_events_subscription_feature_status_created_at
    ON usage_events (subscription_id, feature_id, status, created_at);

CREATE INDEX IF NOT EXISTS idx_usage_event_contexts_usage_event
    ON usage_event_contexts (usage_event_id);

CREATE INDEX IF NOT EXISTS idx_usage_event_contexts_source
    ON usage_event_contexts (event_source, source_id);

CREATE INDEX IF NOT EXISTS idx_subscriptions_candidate_active_window
    ON subscriptions (candidate_id, status, start_date, end_date, purchased_at, id);

CREATE INDEX IF NOT EXISTS idx_subscriptions_company_active_window
    ON subscriptions (company_id, status, start_date, end_date, purchased_at, id);
