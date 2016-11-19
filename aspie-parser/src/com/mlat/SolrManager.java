package src.com.mlat;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import java.net.MalformedURLException;


public class SolrManager {
    //
    public static SolrDocumentList getSolrDocumentList() throws MalformedURLException, SolrServerException{
        HttpSolrServer solr = new HttpSolrServer("http://localhost:8983/solr/mlat");
        SolrQuery query = new SolrQuery();
        query.setQuery("*:*");
        query.setFields("title", "url", "content");
        query.setStart(0);
        query.setRows(14763);
        QueryResponse response = solr.query(query);
        SolrDocumentList results = response.getResults();
        return results;
    }
}
