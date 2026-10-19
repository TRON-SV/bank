CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    currency CHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_accounts_account_number
        UNIQUE (account_number),

    CONSTRAINT ck_accounts_currency_format
        CHECK (currency ~ '^[A-Z]{3}$'),

    CONSTRAINT ck_accounts_status
        CHECK (status IN ('ACTIVE', 'BLOCKED', 'CLOSED'))
);