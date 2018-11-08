# Proxy

A simple proxy for mapping hunger requests for the same search session consistently to the same instance/implementation of a live hunger service. 

The proxy has two main goals:

* A/B Testing Support
* Active/Passive Deployment Support


## A/B (Multivariate) Testing

We'd like an architecture that supports easy testing/comparison of models. AB testing is not just an analytics/data science problem. Yes, you need to process some analytics to see which model has performed better based on your metric of choice, but before you can process those analytics, you first have to perform your experiments by routing actual user requests to different models in a controlled manner. 

The proxy should support a configurable mechanism for defining one or more live hunger service endpoints and assigning weights to each end point such that new sessions will be uniformly randomly routed to one of the assigned end point and existing sessions will consistently be routed to the same endpoint each time a new request comes through.

## Active/Passive Deployment

When a new group of models is deployed at a new set of end points, we want to continue to route existing search sessions to the old end points for a limited period of time so as not to interrupt those sessions. We can say that the new group of end points act as the active model group and the old set is still available as a passive group until it may at some later time be suspended. 

This should also support a rollback mechanism for quickly rolling back to the passive model group in the event that there is some problem with the active model group -- meaning that the passive model group would once again become the active group. 

## Configuration Mechanism

New model groups should be configured and activated by dropping a yaml file into a configuration folder. The proxy will automatically load from model group configurations from that folder and activate them.

## Design Thoughts

Camel routes seem to support a lot of what we want to do here. 

* File endpoint -- "file:model-config" Use a file endpoint to automatically read files dropped into the model-config folder. Unmarshal them with yaml and send them to a processor to activate a new group and render the old group passive.
    * rollbacks -- can we just drop a rollback file into the same folder to do this?
    * suspend -- what about dropping suspend files in to shut down passive groups (i.e. to indicate that they are no longer live and can't be supported even passively, thus forcing requests to start new sessions with the active group)
* Slip routes -- camel slip routes provide the ability to specify the end point for routing dynamically as a header on the message. 
    * Basically, a request can come in to the main http endpoint, get sent to a processor which looks at it's search session id and then decorates the message with an appropriate header for forwarding to the actual endpoint.
    * The endpoint that we route to should be determined based on weather a search session is already present in the request query param header, weather it points to a live (not suspended) model group, and finally which model within that group it points to. 
    * It would be nice if all of that was determined by some simple math on the searchSession id so that we don't have to add too many additional headers -- for instance mod the search session id by the total of all weights in the model group to determine which model it points to and assign ranges of search session ids to each model group to see if it points to a live group. 

    