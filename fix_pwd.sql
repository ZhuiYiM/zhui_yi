UPDATE admin_user SET password = '$2a$10$tJWHUutSzAUp.lvndcZ4l.7i6UMmpx3Uto8q7Tg98t9F.1vSIBBOO' WHERE username = 'admin';
SELECT username, password, LENGTH(password) as pwd_len FROM admin_user WHERE username = 'admin';
