package cn.nightwee.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.util.List;

/**
 * Lucene入门程序
 *
 */
public class LuceneFirst {

    //创建索引
    @Test
    public void testAdd() throws Exception{

        //1.原始文档    MySQL数据库
        //2.创建文档
        BookDao bookDao = new BookDaoImpl();
        List<Book> books = bookDao.queryBookList();

        //4.分析词
//        Analyzer analyzer = new StandardAnalyzer();
        Analyzer analyzer = new IKAnalyzer();
        //5.创建索引
        Directory directory = FSDirectory.open(new File("E:\\Project\\mypro\\demoset\\lucene_project\\temp\\index"));
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST,analyzer);
        IndexWriter indexWriter = new IndexWriter(directory,config);

        for (Book book : books) {
            //3.创建文档对象,每个文档包含多个域
            Document doc = new Document();
            //域，三大属性： 是否分词，是否索引，是否存储
            Field idField = new StoredField("id","" + book.getId());
            Field nameField = new TextField("name",book.getName(), Field.Store.YES);//参数3: 要不要保存到索引库
            Field priceField = new FloatField("price",book.getPrice(), Field.Store.YES);
            Field picField = new StoredField("pic",book.getPic());
            Field descField = new TextField("desc",book.getDesc(), Field.Store.YES);

            doc.add(idField);
            doc.add(nameField);
            doc.add(priceField);
            doc.add(picField);
            doc.add(descField);

            indexWriter.addDocument(doc);
        }

        //关流
        indexWriter.close();
    }

    //查询
    @Test
    public void testFind() throws Exception{
        //查询索引
        Directory directory = FSDirectory.open(new File("E:\\Project\\mypro\\demoset\\lucene_project\\temp\\index"));
        //流:加载过来  (内存)
        IndexReader indexReader = DirectoryReader.open(directory);
        //查询
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //执行查询
        Query query = new TermQuery(new Term("name","java"));
        TopDocs topDocs = indexSearcher.search(query, 5);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            int docId = scoreDoc.doc;
            Document doc = indexReader.document(docId);

            System.out.println("id:" + doc.get("id"));
            System.out.println("name:" + doc.get("name"));
            System.out.println("price:" + doc.get("price"));
            System.out.println("pic:" + doc.get("pic"));
            System.out.println("desc:" + doc.get("desc"));
        }
    }

    //删除
    @Test
    public void testDelete() throws Exception{
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = FSDirectory.open(new File("E:\\Project\\mypro\\demoset\\lucene_project\\temp\\index"));
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST,analyzer);
        IndexWriter indexWriter = new IndexWriter(directory,config);

        //删除全部，慎用
//        indexWriter.deleteAll();
        indexWriter.deleteDocuments(new Term("name","java"));
        indexWriter.close();
    }

    //修改    先删除，再添加
    @Test
    public void testUpdate() throws Exception {
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = FSDirectory.open(new File("E:\\Project\\mypro\\demoset\\lucene_project\\temp\\index"));
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);
        Document doc = new Document();
        //域
        //三大属性:
        //    是否分词  是否索引   是否存储

        //ID   不分        不索     存
        Field idField = new StoredField("ID","6");//参数3:问你 要不要保存到索引库
        //名称  分   索  存
        Field nameField = new TextField("NAME","测试名称", Field.Store.YES);//参数3:问你 要不要保存到索引库
        Field descField = new TextField("DESC","测试内容", Field.Store.NO);//参数3:问你 要不要保存到索引库

        doc.add(idField);
        doc.add(nameField);
        doc.add(descField);

        indexWriter.updateDocument(new Term("name","lucene"),doc);

        indexWriter.close();
    }
}
