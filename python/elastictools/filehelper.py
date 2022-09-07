from asyncore import read
import csv


def enumerate_csv_dict_line(csv_path):
    with open(csv_path, newline='') as csv_file:
        reader = csv.DictReader(csv_file)
        for row in enumerate(reader):
            yield row
