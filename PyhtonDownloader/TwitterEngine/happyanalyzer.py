__author__ = "Andrea Biancini"
__date__ = "November 11, 2013"

from collections import Counter
import Stemmer

import sys
reload(sys)
sys.setdefaultencoding("UTF-8")

import os
root_path = os.path.abspath(os.path.join(__file__, '..', '..'))
lib_path = os.path.join(root_path, 'lib')
sys.path.insert(0, lib_path)

class HappyAnalyzer(object):
  word_list = {}

  def __init__(self, language):
    print "Initializing Happiness Analyzer..."
    dict_path = os.path.abspath(os.path.join(root_path, 'dictionaries'))
    dict_path = os.path.join(dict_path, 'words_%s.txt' % language)
    file_words = open(dict_path, 'r')

    for curline in file_words:
      if '\t' in curline:
        [word, happiness] = curline.split('\t')

        if not word in self.word_list.keys():
          self.word_list[unicode(word)] = float(happiness.replace('\n', ''))

    file_words.close()

  def ScoreTweetHappiness(self, tweet_text):
    try:
      stemmer = Stemmer.Stemmer('french')
      tweet_words = stemmer.stemWords(tweet_text.split(' '))

      total_length = len(tweet_words)
      word_processed = []
      word_frequencies = {}
      word_array = Counter(tweet_words).most_common()
      for (word, freq) in word_array: word_frequencies[word] = freq

      relevant_words = 0.0
      numerator = 0.0
      denominator = 0.0

      for word in tweet_words:
        if not word in word_processed and unicode(word) in self.word_list.keys():
          word_frequency = word_frequencies[word]
          word_happiness = self.word_list[word]
          relevant_words += word_frequency
          # word_frequency = float(word_frequency) / total_length
          numerator += word_happiness * word_frequency
          denominator += word_frequency
          word_processed.append(word)

      happiness = 0.0 if denominator == 0 else numerator / denominator
      relevance = 0.0 if total_length == 0 else relevant_words / total_length

      # print "Computed happiness %d with relevance %.2f." % (happiness, relevance)
      return [round(happiness, 2), round(relevance, 2)]
    except Exception as e:
      print "Received exception during happiness scoring: %s" % e.message
      return [0, 0]

