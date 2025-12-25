-- Users Table (Profile)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    nickname VARCHAR(255),
    avatar VARCHAR(255),
    is_vip BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User Auths Table (Credentials)
CREATE TABLE user_auths (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    identity_type VARCHAR(50) NOT NULL, -- PHONE, WECHAT, ALIPAY
    identifier VARCHAR(255) NOT NULL, -- Phone number or OpenID
    credential VARCHAR(255), -- Password or null
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_time TIMESTAMP,
    UNIQUE(identity_type, identifier)
);

-- Login Logs
CREATE TABLE login_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    terminal VARCHAR(50),
    ip VARCHAR(50),
    login_type VARCHAR(50),
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Lessons Table
CREATE TABLE lessons (
    id BIGSERIAL PRIMARY KEY,
    character VARCHAR(1) NOT NULL,
    pinyin VARCHAR(255),
    definition TEXT
);

-- Lesson Styles Table
CREATE TABLE lesson_styles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    img_url VARCHAR(255),
    lesson_id BIGINT REFERENCES lessons(id)
);

-- Learning Records Table
CREATE TABLE learning_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    lesson_id BIGINT REFERENCES lessons(id),
    learned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
