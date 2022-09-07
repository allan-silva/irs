#!/usr/bin/env python
import sys
import argparse
import elastictools.csvimporter as importer


IMPORTER = "import"
PROGRAM_CHOICES = [IMPORTER]


def parse_args(argv):
    parser = argparse.ArgumentParser(
        description="Elasticsearch learn helper tools"
    )
    parser.add_argument(
        "program",
        help="Operation",
        choices=PROGRAM_CHOICES,
    )
    return parser.parse_args(args=argv)


def run():
    arguments = parse_args(sys.argv[1:2])
    if arguments.program == IMPORTER:
        importer.main(sys.argv[2:])


__name__ == '__main__' and run()
