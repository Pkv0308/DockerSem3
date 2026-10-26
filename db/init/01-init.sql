CREATE TABLE IF NOT EXISTS users (
  user_id       VARCHAR(255) PRIMARY KEY,
  username      VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role          VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS items (
  item_id        VARCHAR(255) PRIMARY KEY,
  name           VARCHAR(255) NOT NULL,
  price          DOUBLE PRECISION NOT NULL,
  stock_quantity INTEGER NOT NULL
);

INSERT INTO users (user_id, username, password_hash, role) VALUES
 ('usr_admin01', 'admin_bob', 'admin123', 'ADMIN'),
 ('usr_emp01', 'emp_alice', 'emp123', 'EMPLOYEE')
ON CONFLICT DO NOTHING;

INSERT INTO items (item_id, name, price, stock_quantity) VALUES
 ('itm_00000001', 'ThinkPad T14', 1200, 10),
 ('itm_00000002', 'Logitech Wireless Mouse', 45, 50),
 ('itm_00000003', 'Mechanical Keyboard', 85, 20)
ON CONFLICT DO NOTHING;