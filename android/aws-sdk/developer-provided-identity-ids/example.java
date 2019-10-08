//You might need to call AWSMobileClient.getInstance().initializeIfNecessary(getApplicationContext(),<Callback>); when your application first runs
//Create an instance of your Developer Provider

//I'm utilizing an AsyncTask because there are network calls and I want to wait (in my use case) for the response before continuing



public class MyClass extends Activity implements AppSyncSubscriptionCall.Callback, ConflictResolverInterface{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        //AWS SDK itself
        com.amazonaws.mobile.AWSMobileClient.initializeMobileClientIfNecessary(getApplicationContext()); //AWS SDK
        //This is the AWS App Sync Client
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                Log.w("User State", result.getUserState().toString());
                initialize();
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void initialize(){
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    DeveloperAuthenticationProvider developerProvider = new DeveloperAuthenticationProvider("unique user id", AWSConfiguration.AMAZON_COGNITO_IDENTITY_POOL_ID, Regions.US_EAST_1); //Configure your region accordinly
                    CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(getApplicationContext(), developerProvider, Regions.US_EAST_1); //Change region Accordinly
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put(developerProvider.getProviderName(), "unique user id");
                    AWSMobileClient.defaultMobileClient().getIdentityManager().setCredentialsProvider(getApplicationContext, credentialsProvider);
                    defaultMobileClient().getIdentityManager().federateWithProvider(defaultMobileClient().getIdentityManager().getCurrentIdentityProvider());
                    return (!AWSMobileClient.getInstance().getIdentityId().equals(""));
                    //For some odd reason, I need to call .getIdentityId() or else the rest fails
                } catch (Exception e) {
                    //Handle exceptions your own way, this is just for the sake of the example
                    e.printStackTrace
                }
                return false;
            }
        
            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                //Because I had to call getIdentityId() to ensure that the SDK is initialize, let's go back to a Thread
                new Thread(() -> {
                    try {
                        //Initialize AWS AppSync with Developer Provided Identity ID
                        mAWSAppSyncClient = AWSAppSyncClient.builder()
                                .context(getApplicationContext())
                                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                                .credentialsProvider(AWSMobileClient.defaultMobileClient().getIdentityManager().getUnderlyingProvider())
                                .useClientDatabasePrefix(true) //IMPORTANT!
                                .conflictResolver(this) 
                                .region(Regions.US_EAST_1)
                                .build();
                        updateMerchant();
                        OnCreateTodosSubscription subscription = OnCreateTodosSubscription.builder().build();
                        subscriptionWatcher = mAWSAppSyncClient.subscribe(subscription);
                        subscriptionWatcher.execute(this);
                        }).start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }.execute();
    }

} 



