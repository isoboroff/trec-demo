import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.FSDirectory;

/** Simple command-line based search demo. */
public class BatchSearch {

	private BatchSearch() {}

	/** Simple command-line based search demo. */
	public static void main(String[] args) throws Exception {
		long startTime = System.nanoTime();
		String usage =
				"Usage:\tjava BatchSearch [-index dir] [-simfn similarity] [-field f] [-queries file]";
		if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
			System.out.println(usage);
			System.out.println("Supported similarity functions:\ndefault: DefaultSimilary (tfidf)\n");
			System.exit(0);
		}

		String index = "index";
		String[] fields = {"contents", "title"};
		Map <String, Float> weights = new HashMap<String, Float>();
		weights.put("contents", 0.4f);
		weights.put("title", 0.6f);
		String queries = null;
		String simstring = "default";

		for(int i = 0;i < args.length;i++) {
			if ("-index".equals(args[i])) {
				index = args[i+1];
				i++;
			} else if ("-field".equals(args[i])) {
				//fields.append(args[i+1]);
				i++;
			} else if ("-queries".equals(args[i])) {
				queries = args[i+1];
				i++;
			} else if ("-simfn".equals(args[i])) {
				simstring = args[i+1];
				i++;
			}
		}

		Similarity simfn = null;
		if ("default".equals(simstring)) {
			simfn = new ClassicSimilarity();
		} else if ("bm25".equals(simstring)) {
			simfn = new BM25Similarity();
		} else if ("dfr".equals(simstring)) {
			simfn = new DFRSimilarity(new BasicModelP(), new AfterEffectL(), new NormalizationH2());
		} else if ("lm".equals(simstring)) {
			simfn = new LMDirichletSimilarity();
		}
		if (simfn == null) {
			System.out.println(usage);
			System.out.println("Supported similarity functions:\ndefault: DefaultSimilary (tfidf)");
			System.out.println("bm25: BM25Similarity (standard parameters)");
			System.out.println("dfr: Divergence from Randomness model (PL2 variant)");
			System.out.println("lm: Language model, Dirichlet smoothing");
			System.exit(0);
		}
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index).toPath()));
		IndexSearcher searcher = new IndexSearcher(reader);
		searcher.setSimilarity(simfn);
		Analyzer analyzer = new CustomAnalyzer();
		
		BufferedReader in = null;
		if (queries != null) {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(queries), "UTF-8"));
		} else {
			in = new BufferedReader(new InputStreamReader(new FileInputStream("queries"), "UTF-8"));
		}
		QueryParser parser = new QueryParser("contents", analyzer);
		while (true) {
			String line = in.readLine();

			if (line == null || line.length() == -1) {
				break;
			}

			line = line.trim();
			if (line.length() == 0) {
				break;
			}
			
			String[] pair = line.split(" ", 2);
			String qs = buildQueryString(pair[1].trim().replaceAll(" +", " "), true);
			Query query = parser.parse(qs);
			// System.out.println(qs);
			doBatchSearch(in, searcher, pair[0], query, simstring);
		}
		reader.close();
		long endTime   = System.nanoTime();
		long totalTime = endTime - startTime;
		//System.out.printf("Query Processing time: %.4fs\n", (float)totalTime/1000000000);
	}

	public static String buildQueryString(String query, boolean conjunctive) {
		if(!conjunctive) {
			return "contents:" + query;
		} else {
			String val = "";
			for(String term : query.split(" ")) {
				val += "contents:" + term + " AND ";
			}
			val = val.replaceAll(" AND $","");
			return val;
		}
	}
	/**
	 * This function performs a top-1000 search for the query as a basic TREC run.
	 */
	public static void doBatchSearch(BufferedReader in, IndexSearcher searcher, String qid, Query query, String runtag)	 
			throws IOException {

		// Collect enough docs to show 5 pages
		TopDocs results = searcher.search(query, 1000);
		ScoreDoc[] hits = results.scoreDocs;
		HashMap<String, String> seen = new HashMap<String, String>(1000);
		int numTotalHits = (int)results.totalHits;
		
		int start = 0;
		int end = Math.min(numTotalHits, 1000);

		for (int i = start; i < end; i++) {
			Document doc = searcher.doc(hits[i].doc);
			String docno = doc.get("docno");
			// There are duplicate document numbers in the FR collection, so only output a given
			// docno once.
			if (seen.containsKey(docno)) {
				continue;
			}
			seen.put(docno, docno);
			System.out.println(qid+" Q0 "+docno+" "+i+" "+hits[i].score+" "+runtag);
		}
	}
}

