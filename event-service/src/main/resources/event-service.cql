CREATE TABLE IF NOT EXISTS mycompany.user_events (
  userId bigint,
  datetime timestamp,
  type text,
  data text,
  primary key (userId, datetime)
)
WITH CLUSTERING ORDER BY (datetime ASC);