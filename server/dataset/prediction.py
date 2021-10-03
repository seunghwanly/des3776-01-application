import csv, sys


def create_dictionary():
    """
    returns dictionary and the keys will be `SNP`
    which makes indexing time complexity to be O(1)

    since we need the `P.VAL`, not going to save that other column information
    - 10.03
    """
    result = {}
    with open('./itrc_snp_hypertension_sm.csv', newline='') as reference_file:
        reader = csv.DictReader(reference_file)
        # column names in itrc_snp_hypertension_sm.csv
        # SNP	CHR	PHENOTYPE	BETA.OR.	P.VAL	BP	minor	major
        for row in reader:
            if row['SNP'] not in result:
                result[row['SNP']] = row['P.VAL']
    return result


def evaluate_testcase(input_file, reference):
    """
    returns count, max p.value and min p.value
    needs the input file to make it evaluated
    """
    # init dict
    geno0 = {'cnt': 0, 'max_p': -1.0, 'min_p': float(sys.maxsize)}
    geno1 = {'cnt': 0, 'max_p': -1.0, 'min_p': float(sys.maxsize)}
    geno2 = {'cnt': 0, 'max_p': -1.0, 'min_p': float(sys.maxsize)}

    with open(input_file, newline='') as test_file:
        reader = csv.reader(test_file)
        # index equals 0:CHR, 1:SNP, 2:geno
        for row in reader:
            _chr, _snp, _geno = row[0], row[1], row[2]
            # check in ref, reference has data with key(SNP), val(P.VAL)
            if _snp in reference:
                p_val = float(reference[_snp])
                if _geno == '0':
                    geno0['cnt'] += 1
                    geno0['max_p'] = max(geno0['max_p'], p_val)
                    geno0['min_p'] = min(geno0['min_p'], p_val)
                elif _geno == '1':
                    geno1['cnt'] += 1
                    geno1['max_p'] = max(geno1['max_p'], p_val)
                    geno1['min_p'] = min(geno1['min_p'], p_val)
                elif _geno == '2':
                    geno2['cnt'] += 1
                    geno2['max_p'] = max(geno2['max_p'], p_val)
                    geno2['min_p'] = min(geno2['min_p'], p_val)
    print('process done.')
    return geno0, geno1, geno2


if __name__ == "__main__":
    ref = create_dictionary()
    g0, g1, g2 = evaluate_testcase('./test_set1.csv', ref)
    print(g0)
    print(g1)
    print(g2)
