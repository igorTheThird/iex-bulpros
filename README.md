
# PRODUCION READY

    In order for this project to be production ready, there are a couple of things that needs to be done.

    First thing that needs to be completed is Database layer, 
    which is now implemented as a List  (class in question com.bulpros.util.Db).
    It was implemented that way so that it can serve as a database layer, but for now just
    easyer implementation was needed.
    
    Second thing, tests. Few test were writen just for show, but further testing is required
    for project to be Production ready.
    
    Service1 which serve as a data getter from IEX platform and stores data in db layer, 
    needs to be expanded further to use all the benefits of the platform. 
    Now is implemented just for History Prices IEX endpoint.
    
    Exceptions needs to be writen more precisely, for now they are just for show. And more 
    exceptions are needed.

    Regarding Integration testing it can't be done with sandbox because the data as in IEX documentation
    can be changed (which we can see when logo url is requested, data is dirted).


# MANUAL TESTS 

    PHASE 1 ---------------------------------------------------------------------------------------

    http://localhost:4567/iex/company/aapl/20190401?chartByDay=true
    http://localhost:4567/iex/company/fb/20190604/20190620

    http://localhost:4567/iex/companies/20190228?chartByDay=true&companies=aapl&companies=fb
    http://localhost:4567/iex/companies/20190228/20190320?companies=aapl&companies=fb


    PHASE 2 ----------------------------------------------------------------------------------------

    http://localhost:4567/bulpros/company/Apple/20190401
    http://localhost:4567/bulpros/company/Facebook/20190301/20190320

    http://localhost:4567/bulpros/companies/20190228?companies=Apple&companies=Facebook
    http://localhost:4567/bulpros/companies/20190606/20190619?companies=Apple&companies=Facebook
