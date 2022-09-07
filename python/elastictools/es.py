from elasticsearch import Elasticsearch


class ElasticIndexClient:
    def __init__(self, index, host="http://localhost:9200"):
        self.index = index
        self.client = Elasticsearch(hosts=host)

    def index_doc(self, doc_id, doc):
        self.client.index(index=self.index, document=doc, id=doc_id)
