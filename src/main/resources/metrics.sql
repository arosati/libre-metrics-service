
DROP TABLE public.sample_metric;
DROP TABLE public.sample;
DROP TABLE public.metric;
DROP TABLE public.service;
DROP TABLE public.agent;

CREATE TABLE public.agent (
       id SERIAL NOT NULL
     , user_agent VARCHAR
     , ip_address CHAR(15)
     , session_id VARCHAR
     , host VARCHAR
     , PRIMARY KEY (id)
);

CREATE TABLE public.service (
       id SERIAL NOT NULL
     , service VARCHAR(45)
     , instance VARCHAR(80)
     , sponsor VARCHAR(80)
     , PRIMARY KEY (id)
);

CREATE TABLE public.metric (
       id SERIAL NOT NULL
     , name VARCHAR(128)
     , value TEXT
     , public BOOLEAN
     , PRIMARY KEY (id)
);

CREATE TABLE public.sample (
       id SERIAL NOT NULL
     , agent_id INTEGER
     , service_id INTEGER
     , entry_ts TIMESTAMP WITH TIME ZONE
     , PRIMARY KEY (id)
     , CONSTRAINT FK_agent_id FOREIGN KEY (agent_id)
                  REFERENCES public.agent (id)
     , CONSTRAINT FK_service_id FOREIGN KEY (service_id)
                  REFERENCES public.service (id)
);

CREATE TABLE public.sample_metric (
       sample_id INTEGER NOT NULL
     , metric_id INTEGER NOT NULL
     , PRIMARY KEY (sample_id, metric_id)
     , CONSTRAINT FK_sample_id FOREIGN KEY (sample_id)
                  REFERENCES public.sample (id)
     , CONSTRAINT FK_metric_id FOREIGN KEY (metric_id)
                  REFERENCES public.metric (id)
);

