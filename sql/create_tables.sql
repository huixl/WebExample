-- 创建用户账户表
CREATE TABLE t_account (
    id BIGSERIAL PRIMARY KEY,
    nick_name VARCHAR(50),
    real_name VARCHAR(50),
    gender VARCHAR(10) CHECK (gender IN ('male', 'female', 'other')),
    birth_date DATE,
    avatar_url VARCHAR(255),
    bio TEXT,
    `address` TEXT,
    country VARCHAR(50),
    city VARCHAR(50) ,
    postal_code VARCHAR(20) ,
    account_status INTEGER DEFAULT 0 ,
    verification_status INTEGER DEFAULT 0,
    verification_type VARCHAR(20),
    verification_id VARCHAR(50),
    verified_at TIMESTAMP,
    lock_reason VARCHAR(100),
    locked_at TIMESTAMP,
    lock_expires_at TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 添加列注释
COMMENT ON TABLE account IS '用户账户表';
COMMENT ON COLUMN account.id IS '账户表主键ID';
COMMENT ON COLUMN account.nick_name IS '用户昵称';
COMMENT ON COLUMN account.real_name IS '真实姓名';
COMMENT ON COLUMN account.gender IS '性别';
COMMENT ON COLUMN account.birth_date IS '出生日期';
COMMENT ON COLUMN account.avatar_url IS '头像URL';
COMMENT ON COLUMN account.bio IS '个人简介';
COMMENT ON COLUMN account.address IS '地址';
COMMENT ON COLUMN account.country IS '国家';
COMMENT ON COLUMN account.city IS '城市';
COMMENT ON COLUMN account.postal_code IS '邮政编码';
COMMENT ON COLUMN account.account_status IS '账户状态 0-正常 1-锁定 2-禁用';
COMMENT ON COLUMN account.verification_status IS '验证状态 0-未验证 1-已验证';
COMMENT ON COLUMN account.verification_type IS '实名认证类型：ID_CARD-身份证，PASSPORT-护照，OTHER-其他';
COMMENT ON COLUMN account.verification_id IS '实名认证证件号码';
COMMENT ON COLUMN account.verified_at IS '实名认证时间';
COMMENT ON COLUMN account.lock_reason IS '账户锁定原因';
COMMENT ON COLUMN account.locked_at IS '账户锁定时间';
COMMENT ON COLUMN account.lock_expires_at IS '账户锁定过期时间';
COMMENT ON COLUMN account.create_time IS '创建时间';
COMMENT ON COLUMN account.update_time IS '更新时间';

-- 创建登录方式字典表
CREATE TABLE t_login_type (
    id SERIAL PRIMARY KEY,
    type_code VARCHAR(50) NOT NULL UNIQUE,
    type_name VARCHAR(100) NOT NULL,
    max_attempts INTEGER DEFAULT 5,
    is_enabled INTEGER DEFAULT 0,
    sort_order INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 添加列注释
COMMENT ON TABLE login_type IS '登录方式字典表';
COMMENT ON COLUMN login_type.id IS '登录方式ID';
COMMENT ON COLUMN login_type.type_code IS '登录方式代码';
COMMENT ON COLUMN login_type.type_name IS '登录方式名称';
COMMENT ON COLUMN login_type.max_attempts IS '尝试最大限制';
COMMENT ON COLUMN login_type.is_enabled IS '是否启用 0-启用 1-禁用';
COMMENT ON COLUMN login_type.sort_order IS '排序顺序';
COMMENT ON COLUMN login_type.create_time IS '创建时间';
COMMENT ON COLUMN login_type.update_time IS '更新时间';

-- 创建用户登录表
CREATE TABLE t_login (
    id BIGSERIAL PRIMARY KEY,
    identity VARCHAR(255) NOT NULL,
    credential VARCHAR(255) NOT NULL,
    login_type_id INTEGER NOT NULL,
    last_login_time TIMESTAMP,
    login_attempts INTEGER DEFAULT 0,
    account_locked INTEGER DEFAULT 0 ,
    account_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(id),
    FOREIGN KEY (login_type_id) REFERENCES login_type(id)
);

-- 添加列注释
COMMENT ON TABLE login IS '用户登录表';
COMMENT ON COLUMN login.id IS '登录表主键ID';
COMMENT ON COLUMN login.identity IS '认证标识：邮箱、手机号、用户名';
COMMENT ON COLUMN login.credential IS '认证凭证：密码、验证码';
COMMENT ON COLUMN login.login_type_id IS '登录方式ID';
COMMENT ON COLUMN login.last_login_time IS '最后登录时间';
COMMENT ON COLUMN login.login_attempts IS '登录尝试次数';
COMMENT ON COLUMN login.account_locked IS '账户是否锁定 0-未锁定 1-已锁定';
COMMENT ON COLUMN login.account_id IS '关联的账户ID';
COMMENT ON COLUMN login.create_time IS '创建时间';
COMMENT ON COLUMN login.update_time IS '更新时间';

-- 为account表添加索引
CREATE INDEX idx_account_nickname ON account(nick_name);
CREATE INDEX idx_account_realname ON account(real_name);
CREATE INDEX idx_account_status ON account(account_status);
CREATE INDEX idx_account_verification ON account(verification_status, verification_type);
CREATE INDEX idx_account_verification_id ON account(verification_id);
CREATE INDEX idx_account_created ON account(create_time);

-- 为login表添加索引
CREATE UNIQUE INDEX uk_login_identity_type ON login(identity, login_type_id);
CREATE INDEX idx_login_account ON login(account_id);
CREATE INDEX idx_login_type ON login(login_type_id);
CREATE INDEX idx_login_status ON login(account_locked);
CREATE INDEX idx_login_lastlogin ON login(last_login_time);

-- 为login_type表添加索引
CREATE INDEX idx_logintype_enabled ON login_type(is_enabled);
CREATE INDEX idx_logintype_sort ON login_type(sort_order);

-- 创建更新时间触发器函数
CREATE OR REPLACE FUNCTION update_update_time_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 为每个表添加更新时间触发器
CREATE TRIGGER update_account_update_time
    BEFORE UPDATE ON account
    FOR EACH ROW
    EXECUTE FUNCTION update_update_time_column();

CREATE TRIGGER update_login_type_update_time
    BEFORE UPDATE ON login_type
    FOR EACH ROW
    EXECUTE FUNCTION update_update_time_column();

CREATE TRIGGER update_login_update_time
    BEFORE UPDATE ON login
    FOR EACH ROW
    EXECUTE FUNCTION update_update_time_column();

-- 插入初始登录方式数据
INSERT INTO login_type (type_code, type_name, sort_order) VALUES
('username_password', '用户名密码登录', 1),
('email_code', '邮箱验证码登录', 2),
('phone_code', '手机验证码登录', 3),
('wechat', '微信登录', 4),
('github', 'GitHub登录', 5),
('google', 'Google登录', 6);