Information Retrieval Evaluation Demo
========================

This project is a demonstration of a very simple IR evaluation experiment.  It includes code built on top of Lucene to index a collection and run a set of queries against that collection in batch mode.  Those outputs can then be measured against the true answers.

I wrote this demo for a short course on IR evaluation.  It is only meant as a working example for pedagogical purposes.  It is based on the open-source Lucene toolkit, and is designed to work around a frequently cited collection from the TREC evaluations.

Ingredients
---------

The project here contains all the source code and much of the data you need to compile and run the experiment, along with libraries.  The code uses [Apache Lucene](http://lucene.apache.org), and the code I've contributed here is frankly just hacks from the Lucene demo code.  Run `ant` to compile.  You'll need a JVM v6 or later.

There are three main Java classes:

* `IndexTREC`: this is a program to index the TREC 8 document collection (described more below).  IndexTREC uses the TrecDocIterator class to read the collection files and cut them up into documents with usable references ("docnos").
* `SearchFiles`: this is an adaptation of the Lucene SearchFiles demo, which provides a simple interactive search interface with paging.  There's not much to see, this is mainly for sanity-checking.
* `BatchSearch`: this does a batch experiment.  You give it a set of queries, it gives you ranked lists of documents and scores for each query.

If you want to learn about TREC, I recommend the book from MIT Press, "TREC: Experiment and Evaluation in Information Retrieval" by Voorhees and Harman.  All the proceedings from the TREC meetings are online at (http://trec.nist.gov/).  The [overview from TREC-8](http://trec.nist.gov/pubs/trec8/papers/overview_8.pdf) gives details on the data used here, but perhaps may miss some larger context.

Preparation
---------

In addition to this github, you will need a set of documents to search.  The experiment packaged here uses [TREC CDs 4 and 5](http://trec.nist.gov/data/docs_eng.html), which have been widely used in the information retrieval literature.  I can't distribute the documents with the project, because they are copyrighted.

Once you have CDs 4 and 5, move the Congressional Record subcollection (CR) off to the side someplace and run:

`java -cp "bin:lib/*" IndexTREC -docs /path/to/cd45/documents`

This will create a directory called `index` which is the searchable Lucene index of the documents.  This index is just all the raw terms in all the documents, including markup, treated equally.  On my laptop this takes five minutes from within Eclipse.

You only need the documents if you'd like to actually read them, for example in the course of deeper analysis of this experiment.  If you don't care about seeing the actual documents, you can download a ready-to-use index from (https://ir.nist.gov/cd45-cr.index.tar.gz).  The index is roughly 450MB and so is not distributed via Github.

The project's `test-data` directory contains three data files:

* `topics.301-450`:  These are TREC search topics from TRECs 6, 7, and 8 ad hoc tracks.  These are statements of information need.
* `title-queries.301-450`: These are queries built from the `topics` in the simplest, most dunderheaded way possible --- the query is the words in the `title` field of the topic, with all punctuation replaced with whitespace.
* `qrels.trec6-8.nocr`; These are the correct (and known incorrect) answers for each topic.

Cooking
-------

The `BatchSearch` driver contains support for four `Similarity` models from Lucene.  A Similarity is a method for calculating a score between a query and a document.  In this experiment, we will run the same queries against the same index, using each of the four models, and see if we can observe any differences in effectiveness among them.

The four models are:

1. `default`: this is Lucene's default similarity model, also sometimes called TF-IDF.
2. `bm25`: this is a probabilistic model called BM25, with standardized parameter settings that were determined in TREC-3 and may not be optimal for TREC 6-8.
3. `dfr`: this is a Divergence from Randomness model, specifically one referred to in the literature as PL2.
4. `lm`: this is a Language Model with Dirichlet smoothing.

To run each model, use the following command:

`java -cp "bin:lib/*" BatchSearch -index /path/to/index -queries /path/to/title-queries.301-450` -simfn default > default.out`

substituting 'bm25', 'dfr', or 'lm' for 'default' to run each variant and catch the output in different files.  Each output file will contain up to 1000 documents retrieved from the index for each of the 150 queries.  It takes about a minute on my laptop to run each variant.

In the `test-data` directory are the output files I get when I do the above.  Yours should match unless you indexed your own copy of CDs 4 and 5, and did it differently than I did.

Presentation
----------

You can compare the output files any way you like, but the common way is to compute some numerical measures such as recall and precision based on the positions of relevant and nonrelevant documents in the output files.  The standard tool for this is [trec_eval](https://github.com/usnistgov/trec_eval/).  You'll need a C compiler like gcc to build the tool, but no other infrastructure such as libraries.

To compute the evaluation measures, do

`trec_eval -q -M1000 /path/to/qrels.trec6-8.nocr /path/to/an/output/file`

This will produce a number of quality measures for each individual query, and summary averages.  You will want to refer to the TREC resources linked above to make sense of that.
The files `default.eval`, `lm.eval` etc. in the `test-data` directory include my output from `trec_eval` and should match what you get.

Caveats
-------

This represents in no way shape or form a reasonable evaluation or measurement of Lucene as it might be deployed in the wild and used by real users.  The index is very rudimentary, using no structure from the documents.  The queries are naive.  The queries are not enriched at all before sending them against the index, for example by adding lexical affinities or using pseudorelevance feedback.  There is no search interface guiding the actions of users.

In other words, IT'S A DEMO.  This has been a test of the Emergency Cranfield System.  Had this been an actual comparison of tools configured to perform as state-of-the-art search engines, your mileage would vary considerably.  No warranties expressed, implied, or honored.  If it breaks, you get to keep both pieces.  The management assumes no responsibility.

Lucene is available under an Apache license.  The code here is distributed as the same, as it should be since it is straightforwardly derived from the Lucene codebase.  The TREC data herein is public and freely available from NIST, I have repackaged it somewhat to make things easier for the newbie.

