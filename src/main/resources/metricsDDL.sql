
DROP TABLE public.metric CASCADE;
DROP TABLE public.sample CASCADE;
DROP TABLE public.service CASCADE;

CREATE TABLE public.service (
       id SERIAL NOT NULL
     , service_name VARCHAR(45)
     , instance_name VARCHAR(80)
     , sponsor_name VARCHAR(80)
     , PRIMARY KEY (id)
     , CONSTRAINT UQ_service_1 UNIQUE (service_name, instance_name, sponsor_name)
);

CREATE TABLE public.sample (
       id SERIAL NOT NULL
     , ip_address CHAR(80)
     , session_id VARCHAR(254)
     , host VARCHAR(80)
     , user_agent VARCHAR(254)
     , service_id INTEGER
     , entry_ts TIMESTAMP WITH TIME ZONE
     , PRIMARY KEY (id)
     , CONSTRAINT FK_service_id FOREIGN KEY (service_id)
                  REFERENCES public.service (id)
);

CREATE TABLE public.metric (
       id SERIAL NOT NULL
     , sample_id INTEGER
     , name VARCHAR(128)
     , value TEXT
     , public BOOLEAN default(true)
     , PRIMARY KEY (id)
     , CONSTRAINT FK_sample_id FOREIGN KEY (sample_id)
                  REFERENCES public.sample (id)
);

