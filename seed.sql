INSERT INTO users (user_id, username, password_hash, role) VALUES
 ('usr_admin01', 'admin_bob', 'admin123', 'ADMIN'),
 ('usr_emp01', 'emp_alice', 'emp123', 'EMPLOYEE');
INSERT INTO items (item_id, name, price, stock_quantity) VALUES
 ('itm_00000001', 'ThinkPad T14', 1200, 10),
 ('itm_00000002', 'Logitech Wireless Mouse', 45, 50),
 ('itm_00000003', 'Mechanical Keyboard', 85, 20);
SELECT username, role FROM users;
SELECT name, stock_quantity FROM items;
