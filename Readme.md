CoAP Design Document
    * Notes
        * Uses a trust-based permission scheme
            * Relies on provenance established between SH and Device to
              authenticate
            * ALL device -> SH communication follows this pattern
        * Based on a Publisher / Subscriber messaging distribution model
            * Each device is by default subscribed to itself (denoted by a
              GUID of the device)
            * Pulishers can also define custom subscribable topics
              (Hereinafter 'Topics')
        * Devices on the same network MUST NOT communicate outside of the
          Security Hub.
    * Workflow
        * DeviceX creates message that it needs sent (Hereinafter 'Message')
            * The Mesage schema is defined in the OpenAPI Document
        * Message is posted to the API from the device and is caught by the
          Security Hub's server (Hereinafter 'Server')
            * Server then puts the message in a queue
              (Hereinafter 'Norman')
            * Server then sends an ACK
        * Message is caught by Norman and put in the appropriate secondary
          queue (Hereinafter 'Bucket') to be sent out to the device
            * There is a separate queue for each device, including the security
              hub itself as well as the HTTPs proxy service
            * Norman separates Topics into the buckets of those subscribed
            * Message is also sent to the provenance queue (Hereinafter
              'Kharon') to be recorded
        * Message is queued in Bucket until DeviceY asks for it.
        * Client sends message to DeviceY
            * It is also stored in Kharon to be recoded
        * DeviceY sends ACK to CoAP Client
        * Client sends ACK to DeviceY
        * Security Hub records message in provenance chain
            * Provenance is currently stored Database
            * TODO: Advanced Provenance strategy

                                              Security Hub
                         ------------------------------------------------------
                         |                                   -----------      |
                         |                              x--> | Bucket1 | --|  |
            -----------  |  -----------    -----------  |    -----------   |  |
            |         |  |  |         |    |         |  |    -----------   |  |
            | DeviceX | -|> | Server  | -> | Norman  | -x--> |   ...   | --|  |
            |         |  |  |         |    |         |  |    -----------   |  |
            -----------  |  -----------    -----------  |    -----------   |  |
                         |                              x--> | BucketN | --|  |
            -----------  |  -----------                 |    -----------   |  |
            |         |  |  |         |                 ----------|        |  |
            | DeviceY | <|- | Client  | <-------------------------|--------x  |
            |         |  |  |         |                           v        |  |
            -----------  |  -----------    ------------      -----------   |  |
                         |                 | Database |<---- |  Kharon | <-|  |
                         |                 ------------      -----------      |
                         ------------------------------------------------------