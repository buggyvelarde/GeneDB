/*
  --- menu items --- 
*/
var MENU_ITEMS = [
	['Choose Question', null, null,
		['Genes/Proteins >', null, null,
			['Protein Physical Property >', null, null, 
				['Protein length'],
				['Molecular Weight'],
			],
			['GO Annotation >', null, null,
				['Function >', null, null,
				     ['Any GO function'],
				     ['Particular GO function'],
				],
				['Component >', null, null,
				     ['Any GO component'],
				     ['Particular GO component'],
				],
				['Process >', null, null,
				     ['Any GO process'],
				     ['Particular GO process'],
				],
				['Any GO annotation'],							
			],
			['Domains >', null, null,
				['With specific Pfam'],
				['Any Pfam'],
				['Num. TMM'],
				['Has signal peptide']
			],
			['Expression >', null, null,
				['Elutriation 1'],
				['Elutriation 2']
			],
		],
		['Non-coding RNAs >', null, null,
			['By type']
		],
		['Other features >', null, null,
			['By SO type'],
			['By location']
		],
		['Publications >', null, null,
			['By author'],
			['By systematic id']
		],
	],
];

