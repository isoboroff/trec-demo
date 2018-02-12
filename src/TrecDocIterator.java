import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

public class TrecDocIterator implements Iterator<Document> {

	protected BufferedReader rdr;
	protected boolean at_eof = false;
	
	public TrecDocIterator(File file) throws FileNotFoundException {
		rdr = new BufferedReader(new FileReader(file));
		System.out.println("Reading " + file.toString());
	}
	
	@Override
	public boolean hasNext() {
		return !at_eof;
	}

	@Override
	public Document next() {
		Document doc = new Document();
		StringBuffer sb = new StringBuffer();
		try {
			String line;
			Pattern docno_tag = Pattern.compile("<DOCNO>\\s*(\\S+)\\s*<");
			Pattern title_tag = Pattern.compile("^<HEADLINE>(.*)</HEADLINE>$");
			boolean in_doc = false;
			boolean in_body = false;
			while (true) {
				line = rdr.readLine();
				if (line == null) {
					at_eof = true;
					break;
				}
				if (!in_doc) {
					if (line.startsWith("<DOC>"))
						in_doc = true;
					else
						continue;
				}
				if (line.startsWith("</DOC>")) {
					in_doc = false;
					break;
				}
				if (line.startsWith("<TEXT>")) {
					in_body = true;
					sb.append(line.replaceFirst("^<TEXT>", ""));
					continue;
				}
				if (in_body) {
					if (line.startsWith("</TEXT>")) {
						in_body = false;
						continue;
					}
					sb.append(line);
					continue;
				}

				Matcher m = docno_tag.matcher(line);
				if (m.find()) {
					String docno = m.group(1);
					doc.add(new StringField("docno", docno, Field.Store.YES));
				}

				m = title_tag.matcher(line);
                if (m.find()) {
					String title = m.group(1);
					StringField field = new StringField("title", title, Field.Store.YES);
					doc.add(field);
				}
			}
			if (sb.length() > 0) {
				doc.add(new TextField("contents", sb.toString(), Field.Store.YES));
			}
			
		} catch (IOException e) {
			doc = null;
		}
		return doc;
	}

	@Override
	public void remove() {
		// Do nothing, but don't complain
	}

}
