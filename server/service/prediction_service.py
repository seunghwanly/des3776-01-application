import csv, sys


class HypertensionService:
    def __init__(self, db):
        self.db = db
        self.ref = self.create_ref()

    def create_ref(self):
        """
        returns dictionary and the keys will be `SNP`
        which makes indexing time complexity to be O(1)

        since we need the `P.VAL`, not going to save that other column information
        - 10.03
        """
        db = self.db
        ref = {}
        ref_collection = db.get_collection('ref')
        # SNP	CHR	PHENOTYPE	BETA{OR}	P {VAL}	BP	minor	major
        for res in ref_collection.find():
            if res['SNP'] not in ref:
                ref[res['SNP']] = res['P']['VAL']
        return ref

    def evaluate_testcase(self, input_file):
        """
        returns count, max p.value and min p.value
        needs the input file to make it evaluated
        """
        # init dict
        geno0 = {'cnt': 0, 'max_p': -1.0, 'min_p': float(sys.maxsize)}
        geno1 = {'cnt': 0, 'max_p': -1.0, 'min_p': float(sys.maxsize)}
        geno2 = {'cnt': 0, 'max_p': -1.0, 'min_p': float(sys.maxsize)}

        test_data = input_file.read()
        test_data = str(test_data)[16:-1].split('\\n')

        # index equals 0:CHR, 1:SNP, 2:geno
        for row in test_data:
            row = row.split(',')
            if len(row) == 3:
                _chr, _snp, _geno = row[0], row[1], row[2]
                # check in ref, reference has data with key(SNP), val(P.VAL)
                if _snp in self.ref:
                    p_val = float(self.ref[_snp])
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
        return geno0, geno1, geno2