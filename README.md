# Word-Classification

Train a model that takes a new input word/string and predict which category it belongs to. And generate a list of categories of words.

Using traditional word embedding: 
Define categories of words in a dictionary and use pre-trained word embeddings (Word2Vec or Glove) and build a simple query system to predict the category.
Or use spacy's NER.
We can train word2vec or Glove embeddings on our categories.

Implementation steps:
1. Word Tokenization
2. Stop words removal
3. Create dictionary of words representing to categories
4. Get the word embedding for each word in category
5. Get the word embedding for given text/new sample text wordwise
6. Calculate distance between both words (word in category and word we want to classify) and average them to get the score
7. Map output score with category name and show results


