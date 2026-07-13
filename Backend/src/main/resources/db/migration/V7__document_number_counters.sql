CREATE TABLE document_number_counters (
    doc_type   VARCHAR(30) NOT NULL,
    year       INTEGER     NOT NULL,
    last_value INTEGER     NOT NULL DEFAULT 0,
    PRIMARY KEY (doc_type, year)
);
