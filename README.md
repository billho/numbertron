sg

```json
{	
  "corpusPath" :
"jdbc:derby:/mnt/bag/aman/numbers_corpus",
  "mintzKeywordsFg" :
"",
"numbersFg" :
"",
"keywordsFg" :
"main.java.iitb.neo.pretrain.featuregeneration.KeywordFeatureGenerator",
"countriesList":
"/mnt/a99/d0/aman/MultirExperiments/data/numericalkb/countries_list_ids",
  "kbRelFile" : "/mnt/a99/d0/aman/MultirExperiments/data/numericalkb/kb-worldbank-SI.tsv",
  "sigs" : [
"edu.washington.multirframework.argumentidentification.DefaultSententialInstanceGeneration"
],
  "dsFiles" : [
"/mnt/bag/aman/train/instances_deldanger.tsv"
],
  "featureFiles" : [
"/mnt/bag/aman/train/features_handkeywords.tsv"
],
  "models" : [
"/mnt/bag/aman/model/model_handkeywords" ],
  "cis" :
"edu.washington.multirframework.corpus.DefaultCorpusInformationSpecification",
  "si" : [
"edu.washington.multirframework.corpus.SentNamedEntityLinkingInformation",
"edu.washington.multirframework.corpus.SentFreebaseNotableTypeInformation"
],
  "ti" : [ "edu.washington.multirframework.corpus.TokenChunkInformation" ],
  "di" : [ ],
  "mintzFeatureThreshold" : "1",
  "keywordFeatureThreshold" : "1",
  "regularizer" : "0.999",
  "iterations" : "10",
  "finalAvg" : "false",
  "topKGoldDb" : "5",
  "margin" : "0.20",
  "ignoreConfusion" : "true"
}
```
#Implementation notes; incomplete

##Training Data Preparation

###Finding relevant sentences


The first file, that involves collecting sentences that are relevant can be done
by first creating a file similar to the one created by multir and then
aggregating appropriately.

This creation will now involve additional steps like pruning out sentences that
have modifiers, and checking for units. 

Finally, we will have a file similar to the instances file. There is a crucial
difference between what we do and what multir does however. For us, this is not
distant supervision, just spotting. There is no distant supervision that has
happened yet (In some sense, we already know about the unit of the relation
which can be considered to be a source of distant supervision, but that is it).

###Collecting into Location Number pairs We need to find all the sentences that
possibly express a given relation for a given location. This is easy given the
file above. The first column will give us the location and the last one will
give us hte relation.


###Creating features Again, should be easy. *Each mention* (each location-number
pair) will form one spot, there would be features generated for each spot.
Finally, we'll have a set of spots that are related to a location relation pair.
(For multir, this is done by first creating the feature file and then sorted the
first 2 columns, and picking up the adjoining rows, in our case, we can do
something similar; sort on the first and the last column).

###Notations used by Multir
1. Mappings: The class which stores HashMaps from string to integer, giving a
   number to each of the relations and features.  Only features that exceed a
   certain threshold count are assigned an id, the rest are not. The function
   getMappingsFromTrainingData() is responsible for this portion.

