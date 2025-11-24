-- ============================================
-- PASSWORD RESET TOKENS TABLE
-- ============================================

DROP TABLE IF EXISTS password_reset_tokens CASCADE;

CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    verification_code VARCHAR(10) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,

    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    attempts INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_password_reset_expiry CHECK (expiry_date > created_at)
);

-- ============================================
-- INDEXES
-- ============================================

CREATE INDEX idx_prt_user_id
    ON password_reset_tokens (user_id);

CREATE INDEX idx_prt_verification_code
    ON password_reset_tokens (verification_code);

CREATE INDEX idx_prt_expiry_date
    ON password_reset_tokens (expiry_date);

CREATE INDEX idx_prt_used
    ON password_reset_tokens (used);
