import argparse
from datetime import datetime

from .es import ElasticIndexClient
from .filehelper import enumerate_csv_dict_line


def parse_args(argv):
    parser = argparse.ArgumentParser(
        description="CSV importer tool"
    )
    parser.add_argument(
        "index",
        help="Index name",
    )
    parser.add_argument(
        "csv_file_path",
        help="CSV file path",
    )   
    parser.add_argument(
        "-id",
        help="ID Column",
        required=False
    )
    parser.add_argument(
        "-m",
        "--mapping",
        help="Mapping file",
        required=False
    )
    parser.add_argument(
        "-ds",
        "--data-stream",
        help="Specifies a data stream indexing, automatically adds @timestamp field (now)",
        required=False,
        action="store_true"
    )
    return parser.parse_args(args=argv)


def main(argv):
    arguments = parse_args(argv)
    index_client = ElasticIndexClient(arguments.index)
    for line, content in enumerate_csv_dict_line(arguments.csv_file_path):
        if arguments.data_stream:
            content["@timestamp"] = datetime.now()
        doc_id = content.pop(arguments.id, None)

        try:
            index_client.index_doc(doc_id, content)
            print(f"Imported - Line: {line} - Content: {content}")
        except Exception as e:
            print(f"Error on import - Line: {line} - Content: {content}")
            raise e

