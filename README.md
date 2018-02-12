Authors:

Jonathan Easterman and Gabriel Siqueira

To compile:

`ant`

To index:

`java -cp "bin:lib/*" IndexTREC -docs /path/to/cd45/documents`

To run queries:

java -cp "bin:lib/*" BatchSearch -index index -queries test-data/title-queries.301-450 -simfn [default | bm25] > output_file

To evaluate performance (assuming trec_eval tool is in your PATH):

`trec_eval -mall_trec -M1000 test-data/qrels.trec6-8.nocr output_file`
