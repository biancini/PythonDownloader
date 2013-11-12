__author__ = "Andrea Biancini"
__date__ = "November 11, 2013"

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

  def __init__(self):
    file_path = os.path.dirname(__file__)
    file_path = os.path.join(file_path, 'words_happiness.txt')
    file_words = open(file_path, 'r')

    for curline in file_words:
      if '\t' in curline:
        [word, happiness] = curline.split('\t')

        if not word in self.word_list.keys():
          self.word_list[unicode(word)] = int(happiness.replace('\n',''))

    file_words.close()

  def ScoreTweetHappiness(self, tweet_text):
    stemmer = Stemmer.Stemmer('french')
    tweet_words = stemmer.stemWords(tweet_text.split(' '))

    total_words = 0
    relevant_words = 0
    happiness = 0

    for word in tweet_words:
      if len(word) > 2:
        total_words += 1
        if unicode(word) in self.word_list.keys():
          relevant_words += 1
          happiness += self.word_list[word]

    if relevant_words > 0: happiness = float(happiness) / relevant_words
    if total_words > 0: relevance = round(float(relevant_words) / total_words, 2)

    #print "Computed happiness %d with relevance %.2f." % (happiness, relevance)
    return [happiness, relevance]
