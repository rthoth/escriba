from locust import HttpLocust, TaskSet, task
from random import randint
from tempfile import mkstemp
from filecmp import cmp
import os

colls = ('collA', 'collB', 'woohoo')
colls_limit = len(colls) - 1
def next_collection():
	global colls_limit, colls;
	return colls[randint(0, colls_limit)]

max_keys = 20e9
def next_key():
	global max_keys
	return randint(0, max_keys)

print('Creating data samples...')
samples = []
for d in range(0, 5):
		kb = 1024
		mb = kb * kb
		size = randint(1 * mb,  20 * mb)
		print('Creating sample %d with %d bytes' % (d, size))
		buf = bytearray()
		for i in range(0, size):
			buf.append(randint(1, 255))
		samples.append(buf)

samples_limit = len(samples) - 1
def next_sample():
	global samples
	return samples[randint(0, samples_limit)]

class Putter:
	def __init__(self, client):
		self.client = client
		self.col = next_collection()
		self.key = next_key()
		self.sample = next_sample()

	def put(self):
		print('Putting...')
		with self.client.put('/%s/%d' % (self.col, self.key), data=self.sample, catch_response=True) as response:
			if response.status_code == 201:
				response.success()
			else:
				response.failure('%d/%s' % (response.status_code, response.reason))

		return (self.col, self.key, self.sample)

class Getter(TaskSet):
	@task
	def get(self):
		putter = Putter(self.client)
		(col, key, sample) = putter.put()

		print('Getting %s/%d' % (col, key))

		with self.client.get('/%s/%d' % (col, key), catch_response=True, stream=True) as response:
			if response.status_code == 200:
				if response.content == sample:
					response.success()
				else:
					response.failure('Ops!')
			else:
				response.failure('%d/%s' % (response.status_code, response.reason))

class Stree(HttpLocust):
	task_set = Getter
	max_wait = 10000
	min_wait = 10