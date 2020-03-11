import logging
from rest_framework.renderers import JSONRenderer
from django.http import HttpResponse
from django.template import loader


logger = logging.getLogger(__name__)


def agents_list(request):
    # TODO: make request to MFT server here
    agents_json = {"agents" :[{"id": "s3","name": "Amazon S3",
  "description": "Amazon S3 or Amazon Simple Storage Service is a service offered by Amazon Web Services that provides object storage through a web service interface. Amazon S3 uses the same scalable storage infrastructure that Amazon.com uses to run its global e-commerce network."},
   {"id": "box", "name": "Box",
  "description": "With Box, you get a single place to manage, secure, share and govern all of the content for your internal and external collaboration and processes."}]}
    template = loader.get_template('mft_agents/agents_list.html')
    return HttpResponse(template.render({
        'agents': agents_json
    }, request))


