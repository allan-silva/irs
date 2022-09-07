import argparse
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
    return parser.parse_args(args=argv)


def main(argv):
    arguments = parse_args(argv)
    index_client = ElasticIndexClient(arguments.index)
    for line, content in enumerate_csv_dict_line(arguments.csv_file_path):
        doc_id = content.pop(arguments.id, line)
        index_client.index_doc(doc_id, content)
        print(f"Imported - Line: {line} - Content: {content}")
