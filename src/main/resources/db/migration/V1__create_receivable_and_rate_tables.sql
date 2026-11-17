CREATE TABLE receivables (
    id UNIQUEIDENTIFIER NOT NULL,
    assignor NVARCHAR(200) NOT NULL,
    face_value_brl DECIMAL(38, 2) NOT NULL,
    due_date DATE NOT NULL,
    receivable_type NVARCHAR(32) NOT NULL,
    status NVARCHAR(16) NOT NULL,
    version BIGINT NOT NULL CONSTRAINT df_receivables_version DEFAULT 0,
    created_at DATETIMEOFFSET(7) NOT NULL,
    CONSTRAINT pk_receivables PRIMARY KEY (id),
    CONSTRAINT ck_receivables_face_value_positive CHECK (face_value_brl > 0),
    CONSTRAINT ck_receivables_type CHECK (receivable_type IN ('DUPLICATA_MERCANTIL', 'CHEQUE_PRE_DATADO')),
    CONSTRAINT ck_receivables_status CHECK (status IN ('OPEN', 'SETTLED'))
);

CREATE TABLE base_rates (
    id UNIQUEIDENTIFIER NOT NULL,
    monthly_rate DECIMAL(38, 18) NOT NULL,
    valid_from DATETIMEOFFSET(7) NOT NULL,
    valid_to DATETIMEOFFSET(7) NULL,
    created_at DATETIMEOFFSET(7) NOT NULL CONSTRAINT df_base_rates_created_at DEFAULT SYSUTCDATETIME(),
    CONSTRAINT pk_base_rates PRIMARY KEY (id),
    CONSTRAINT ck_base_rates_validity CHECK (valid_to IS NULL OR valid_to > valid_from)
);

CREATE UNIQUE INDEX ux_base_rates_valid_from ON base_rates(valid_from);
CREATE INDEX ix_base_rates_validity ON base_rates(valid_from, valid_to);

CREATE TABLE exchange_rates (
    id UNIQUEIDENTIFIER NOT NULL,
    source_currency NVARCHAR(3) NOT NULL,
    target_currency NVARCHAR(3) NOT NULL,
    rate DECIMAL(38, 18) NOT NULL,
    valid_from DATETIMEOFFSET(7) NOT NULL,
    valid_to DATETIMEOFFSET(7) NULL,
    created_at DATETIMEOFFSET(7) NOT NULL CONSTRAINT df_exchange_rates_created_at DEFAULT SYSUTCDATETIME(),
    CONSTRAINT pk_exchange_rates PRIMARY KEY (id),
    CONSTRAINT ck_exchange_rates_positive CHECK (rate > 0),
    CONSTRAINT ck_exchange_rates_distinct_currencies CHECK (source_currency <> target_currency),
    CONSTRAINT ck_exchange_rates_currencies CHECK (
        source_currency IN ('BRL', 'USD') AND target_currency IN ('BRL', 'USD')
    ),
    CONSTRAINT ck_exchange_rates_validity CHECK (valid_to IS NULL OR valid_to > valid_from)
);

CREATE UNIQUE INDEX ux_exchange_rates_pair_valid_from
    ON exchange_rates(source_currency, target_currency, valid_from);
CREATE INDEX ix_exchange_rates_pair_validity
    ON exchange_rates(source_currency, target_currency, valid_from, valid_to);
