CREATE TABLE refresh_tokens (
    id serial   PRIMARY KEY,
    user_id     integer             NOT NULL,
    token_hash  varchar(255)        NOT NULL,
    device      varchar(255),
    created_at  timestamp           NOT NULL DEFAULT now(),
    expires_at  timestamp           NOT NULL,

    constraint fk_user_id foreign key (user_id) references users(id) on delete cascade
);

-- optimization

CREATE UNIQUE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);