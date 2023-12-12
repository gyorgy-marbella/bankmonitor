ALTER TABLE transaction
ALTER COLUMN id SET DEFAULT nextval('transaction_id_seq');
