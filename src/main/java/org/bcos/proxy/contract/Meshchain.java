package org.bcos.proxy.contract;

import org.bcos.channel.client.TransactionSucCallback;
import org.bcos.web3j.abi.EventEncoder;
import org.bcos.web3j.abi.EventValues;
import org.bcos.web3j.abi.TypeReference;
import org.bcos.web3j.abi.datatypes.Event;
import org.bcos.web3j.abi.datatypes.Function;
import org.bcos.web3j.abi.datatypes.Type;
import org.bcos.web3j.abi.datatypes.Utf8String;
import org.bcos.web3j.abi.datatypes.generated.Bytes32;
import org.bcos.web3j.abi.datatypes.generated.Int256;
import org.bcos.web3j.abi.datatypes.generated.Uint256;
import org.bcos.web3j.abi.datatypes.generated.Uint8;
import org.bcos.web3j.crypto.Credentials;
import org.bcos.web3j.protocol.Web3j;
import org.bcos.web3j.protocol.core.DefaultBlockParameter;
import org.bcos.web3j.protocol.core.methods.request.EthFilter;
import org.bcos.web3j.protocol.core.methods.response.Log;
import org.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.bcos.web3j.tx.Contract;
import org.bcos.web3j.tx.TransactionManager;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import rx.Observable;
import rx.functions.Func1;

/**
 * Auto generated code.<br>
 * <strong>Do not modify!</strong><br>
 * Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>, or {@link org.bcos.web3j.codegen.SolidityFunctionWrapperGenerator} to update.
 *
 * <p>Generated with web3j version none.
 */
public final class Meshchain extends Contract {
    private static final String BINARY = "6060604052341561000f57600080fd5b611b768061001e6000396000f300606060405236156100a2576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680631de26e16146100a75780633c63da7c146100ef5780634986e1dc1461012a578063adff8b0f14610182578063b7973ad9146101da578063ca79e8bb1461023b578063d2d9f9bd1461039c578063d412b8ef146103d7578063e591f4901461042c578063ffef1df214610481575b600080fd5b34156100b257600080fd5b6100d56004808035600019169060200190919080359060200190919050506105bb565b604051808215151515815260200191505060405180910390f35b34156100fa57600080fd5b61011060048080359060200190919050506107fb565b604051808215151515815260200191505060405180910390f35b341561013557600080fd5b61014f600480803560001916906020019091905050610a57565b60405180856000191660001916815260200184815260200183815260200182815260200194505050505060405180910390f35b341561018d57600080fd5b6101a7600480803560001916906020019091905050610afd565b60405180858152602001848152602001838152602001826000191660001916815260200194505050505060405180910390f35b34156101e557600080fd5b61022160048080356000191690602001909190803590602001909190803560ff1690602001909190803560001916906020019091905050610b9f565b604051808215151515815260200191505060405180910390f35b341561024657600080fd5b610382600480803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803590602001908201803590602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050919080356000191690602001909190803560001916906020019091908035906020019091905050610e31565b604051808215151515815260200191505060405180910390f35b34156103a757600080fd5b6103bd60048080359060200190919050506110ed565b604051808215151515815260200191505060405180910390f35b34156103e257600080fd5b61041260048080356000191690602001909190803560001916906020019091908035906020019091905050611333565b604051808215151515815260200191505060405180910390f35b341561043757600080fd5b61046760048080356000191690602001909190803560001916906020019091908035906020019091905050611602565b604051808215151515815260200191505060405180910390f35b341561048c57600080fd5b6105a5600480803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803590602001908201803590602001908080601f01602080910402602001604051908101604052809392919081815260200183838082843782019150505050505091905050611a09565b6040518082815260200191505060405180910390f35b6000806105c6611aa9565b6000806000876000191660001916815260200190815260200160002060000154600019161415610632577fadc8d5c9125a135a878d45a5418853c911885a244706bcd0077d9ad85947c6376127136040518082815260200191505060405180910390a1600092506107f3565b600080866000191660001916815260200190815260200160002091508382600101600082825401925050819055508160008087600019166000191681526020019081526020016000206000820154816000019060001916905560018201548160010155600282015481600201556003820160009054906101000a900460ff168160030160006101000a81548160ff021916908360ff160217905550600482015481600401906000191690559050506001600560008282540192505081905550608060405190810160405280600554815260200186600019168152602001858152602001600060ff1681525090508060036000600554815260200190815260200160002060008201518160000155602082015181600101906000191690556040820151816002015560608201518160030160006101000a81548160ff021916908360ff1602179055509050507fadc8d5c9125a135a878d45a5418853c911885a244706bcd0077d9ad85947c63760006040518082815260200191505060405180910390a17fad7514c4e1b8a19504ea42b5653b2e4aa0bd53cdd902032cbeac6ffaeb2c86346005546040518082815260200191505060405180910390a1600192505b505092915050565b60008060008060026000868152602001908152602001600020600001541415610860577fadc8d5c9125a135a878d45a5418853c911885a244706bcd0077d9ad85947c6376127236040518082815260200191505060405180910390a160009250610a50565b6002600085815260200190815260200160002091506000808360010154600019166000191681526020019081526020016000209050816003015481600201600082825403925050819055508160030154816001016000828254019250508190555060048260040160006101000a81548160ff021916908360ff1602179055508160026000868152602001908152602001600020600082015481600001556001820154816001019060001916905560028201548160020190600019169055600382015481600301556004820160009054906101000a900460ff168160040160006101000a81548160ff021916908360ff160217905550905050806000808460010154600019166000191681526020019081526020016000206000820154816000019060001916905560018201548160010155600282015481600201556003820160009054906101000a900460ff168160030160006101000a81548160ff021916908360ff160217905550600482015481600401906000191690559050507fadc8d5c9125a135a878d45a5418853c911885a244706bcd0077d9ad85947c63760006040518082815260200191505060405180910390a17f436da5fe6663dbee934508e967fd96b8d88ebd81d4d2b4285d60c0f5f62bddbf846040518082815260200191505060405180910390a1600192505b5050919050565b60008060008060008060016000886000191660001916815260200190815260200160002060000154600019161415610aa8576000806000809291908292508191508090509450945094509450610af5565b60016000876000191660001916815260200190815260200160002090508060000154816001015482600201548360030160009054906101000a900460ff168060ff16905094509450945094505b509193509193565b600080600080600080600080886000191660001916815260200190815260200160002060000154600019161415610b4b57600080600082925081915080905060009450945094509450610b97565b60008087600019166000191681526020019081526020016000209050806001015481600201548260030160009054906101000a900460ff1683600401548160ff16915094509450945094505b509193509193565b6000610ba9611ad8565b600080600088600019166000191681526020019081526020016000206000015460001916141515610c16577fadc8d5c9125a135a878d45a5418853c911885a244706bcd0077d9ad85947c6376127116040518082815260200191505060405180910390a160009150610e28565b60a06040519081016040528087600019168152602001868152602001600081526020018560ff1681526020018460001916815250905060018460ff16148015610c8257506000600160008560001916600019168152602001908152602001600020600001546000191614155b15610cc9577fadc8d5c9125a135a878d45a5418853c911885a244706bcd0077d9ad85947c6376127246040518082815260200191505060405180910390a160009150610e28565b60018460ff16148015610cfe575060006001600085600019166000191681526020019081526020016000206000015460001916145b15610d78578060016000856000191660001916815260200190815260200160002060008201518160000190600019169055602082015181600101556040820151816002015560608201518160030160006101000a81548160ff021916908360ff160217905550608082015181600401906000191690559050505b80600080886000191660001916815260200190815260200160002060008201518160000190600019169055602082015181600101556040820151816002015560608201518160030160006101000a81548160ff021916908360ff160217905550608082015181600401906000191690559050507fadc8d5c9125a135a878d45a5418853c911885a244706bcd0077d9ad85947c63760006040518082815260200191505060405180910390a1600191505b50949350505050565b600080610e3c611b11565b600080600080886000191660001916815260200190815260200160002060000154600019161415610ea9577fadc8d5c9125a135a878d45a5418853c911885a244706bcd0077d9ad85947c6376127136040518082815260200191505060405180910390a1600093506110df565b60008087600019166000191681526020019081526020016000209250600160046000828254019250508190555060a06040519081016040528060045481526020018860001916815260200187600019168152602001868152602001600060ff168152509150610f1a8b8b8b8b611a3b565b9050600081141515610fe9576003826080019060ff16908160ff1681525050816002600060045481526020019081526020016000206000820151816000015560208201518160010190600019169055604082015181600201906000191690556060820151816003015560808201518160040160006101000a81548160ff021916908360ff1602179055509050507fadc8d5c9125a135a878d45a5418853c911885a244706bcd0077d9ad85947c63761271c6040518082815260200191505060405180910390a1600093506110df565b848360010160008282540192505081905550816002600060045481526020019081526020016000206000820151816000015560208201518160010190600019169055604082015181600201906000191690556060820151816003015560808201518160040160006101000a81548160ff021916908360ff1602179055509050507fadc8d5c9125a135a878d45a5418853c911885a244706bcd0077d9ad85947c63760006040518082815260200191505060405180910390a17f436da5fe6663dbee934508e967fd96b8d88ebd81d4d2b4285d60c0f5f62bddbf6004546040518082815260200191505060405180910390a1600193505b505050979650505050505050565b60008060008060026000868152602001908152602001600020600001541415611152577fadc8d5c9125a135a878d45a5418853c911885a244706bcd0077d9ad85947c6376127236040518082815260200191505060405180910390a16000925061132c565b60026000858152602001908152602001600020915060008083600101546000191660001916815260200190815260200160002090508160030154816002016000828254039250508190555060008260040160006101000a81548160ff021916908360ff1602179055508160026000868152602001908152602001600020600082015481600001556001820154816001019060001916905560028201548160020190600019169055600382015481600301556004820160009054906101000a900460ff168160040160006101000a81548160ff021916908360ff160217905550905050806000808460010154600019166000191681526020019081526020016000206000820154816000019060001916905560018201548160010155600282015481600201556003820160009054906101000a900460ff168160030160006101000a81548160ff021916908360ff160217905550600482015481600401906000191690559050507fadc8d5c9125a135a878d45a5418853c911885a244706bcd0077d9ad85947c63760006040518082815260200191505060405180910390a17f436da5fe6663dbee934508e967fd96b8d88ebd81d4d2b4285d60c0f5f62bddbf846040518082815260200191505060405180910390a1600192505b5050919050565b60008061133e611b11565b60008060008860001916600019168152602001908152602001600020600001546000191614156113aa577fadc8d5c9125a135a878d45a5418853c911885a244706bcd0077d9ad85947c6376127136040518082815260200191505060405180910390a1600092506115f9565b60008087600019166000191681526020019081526020016000209150600160046000828254019250508190555060a06040519081016040528060045481526020018760001916815260200186600019168152602001858152602001600060ff16815250905083826001015410156114de576001816080019060ff16908160ff1681525050806002600060045481526020019081526020016000206000820151816000015560208201518160010190600019169055604082015181600201906000191690556060820151816003015560808201518160040160006101000a81548160ff021916908360ff1602179055509050507fadc8d5c9125a135a878d45a5418853c911885a244706bcd0077d9ad85947c6376127166040518082815260200191505060405180910390a1600092506115f9565b8382600101600082825403925050819055508382600201600082825401925050819055506002816080019060ff16908160ff1681525050806002600060045481526020019081526020016000206000820151816000015560208201518160010190600019169055604082015181600201906000191690556060820151816003015560808201518160040160006101000a81548160ff021916908360ff1602179055509050507fadc8d5c9125a135a878d45a5418853c911885a244706bcd0077d9ad85947c63760006040518082815260200191505060405180910390a17f436da5fe6663dbee934508e967fd96b8d88ebd81d4d2b4285d60c0f5f62bddbf6004546040518082815260200191505060405180910390a1600192505b50509392505050565b600080600061160f611b11565b60008060008960001916600019168152602001908152602001600020600001546000191614806116605750600080600088600019166000191681526020019081526020016000206000015460001916145b156116a7577fadc8d5c9125a135a878d45a5418853c911885a244706bcd0077d9ad85947c6376127136040518082815260200191505060405180910390a1600093506119ff565b6000808860001916600019168152602001908152602001600020925060008087600019166000191681526020019081526020016000209150600160046000828254019250508190555060a06040519081016040528060045481526020018860001916815260200187600019168152602001868152602001600060ff16815250905084836001015410156117f7576001816080019060ff16908160ff1681525050806002600060045481526020019081526020016000206000820151816000015560208201518160010190600019169055604082015181600201906000191690556060820151816003015560808201518160040160006101000a81548160ff021916908360ff1602179055509050507fadc8d5c9125a135a878d45a5418853c911885a244706bcd0077d9ad85947c6376127166040518082815260200191505060405180910390a1600093506119ff565b8483600101600082825403925050819055508482600101600082825401925050819055508260008089600019166000191681526020019081526020016000206000820154816000019060001916905560018201548160010155600282015481600201556003820160009054906101000a900460ff168160030160006101000a81548160ff021916908360ff160217905550600482015481600401906000191690559050508160008088600019166000191681526020019081526020016000206000820154816000019060001916905560018201548160010155600282015481600201556003820160009054906101000a900460ff168160030160006101000a81548160ff021916908360ff16021790555060048201548160040190600019169055905050806002600060045481526020019081526020016000206000820151816000015560208201518160010190600019169055604082015181600201906000191690556060820151816003015560808201518160040160006101000a81548160ff021916908360ff1602179055509050507fadc8d5c9125a135a878d45a5418853c911885a244706bcd0077d9ad85947c63760006040518082815260200191505060405180910390a17f436da5fe6663dbee934508e967fd96b8d88ebd81d4d2b4285d60c0f5f62bddbf6004546040518082815260200191505060405180910390a1600193505b5050509392505050565b600080611a1886868686611a67565b9050600081141515611a2e576127219150611a32565b8091505b50949350505050565b6000806000611a48611a93565b91506000806000806000888a8c8e8a2f90508092505050949350505050565b6000806000611a74611a9e565b91506000806000806000888a8c8e8a2f90508092505050949350505050565b600062066668905090565b600062066669905090565b608060405190810160405280600081526020016000801916815260200160008152602001600060ff1681525090565b60a060405190810160405280600080191681526020016000815260200160008152602001600060ff168152602001600080191681525090565b60a06040519081016040528060008152602001600080191681526020016000801916815260200160008152602001600060ff16815250905600a165627a7a723058204233534fdc552ffdec459897d0bb3979416709ae65156879f007b3589fde11d40029";

    private Meshchain(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    private Meshchain(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static List<RetLogEventResponse> getRetLogEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("retLog", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<RetLogEventResponse> responses = new ArrayList<RetLogEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            RetLogEventResponse typedResponse = new RetLogEventResponse();
            typedResponse.code = (Int256) eventValues.getNonIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<RetLogEventResponse> retLogEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("retLog", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, RetLogEventResponse>() {
            @Override
            public RetLogEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                RetLogEventResponse typedResponse = new RetLogEventResponse();
                typedResponse.code = (Int256) eventValues.getNonIndexedValues().get(0);
                return typedResponse;
            }
        });
    }

    public static List<DepLogEventResponse> getDepLogEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("depLog", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<DepLogEventResponse> responses = new ArrayList<DepLogEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            DepLogEventResponse typedResponse = new DepLogEventResponse();
            typedResponse.id = (Uint256) eventValues.getNonIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<DepLogEventResponse> depLogEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("depLog", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, DepLogEventResponse>() {
            @Override
            public DepLogEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                DepLogEventResponse typedResponse = new DepLogEventResponse();
                typedResponse.id = (Uint256) eventValues.getNonIndexedValues().get(0);
                return typedResponse;
            }
        });
    }

    public static List<TransferLogEventResponse> getTransferLogEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("transferLog", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<TransferLogEventResponse> responses = new ArrayList<TransferLogEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            TransferLogEventResponse typedResponse = new TransferLogEventResponse();
            typedResponse.id = (Uint256) eventValues.getNonIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<TransferLogEventResponse> transferLogEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("transferLog", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, TransferLogEventResponse>() {
            @Override
            public TransferLogEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                TransferLogEventResponse typedResponse = new TransferLogEventResponse();
                typedResponse.id = (Uint256) eventValues.getNonIndexedValues().get(0);
                return typedResponse;
            }
        });
    }

    public static List<AssetsLogEventResponse> getAssetsLogEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("assetsLog", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<AssetsLogEventResponse> responses = new ArrayList<AssetsLogEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            AssetsLogEventResponse typedResponse = new AssetsLogEventResponse();
            typedResponse.code = (Int256) eventValues.getNonIndexedValues().get(0);
            typedResponse.availAssets = (Uint256) eventValues.getNonIndexedValues().get(1);
            typedResponse.frozenAssets = (Uint256) eventValues.getNonIndexedValues().get(2);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<AssetsLogEventResponse> assetsLogEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("assetsLog", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, AssetsLogEventResponse>() {
            @Override
            public AssetsLogEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                AssetsLogEventResponse typedResponse = new AssetsLogEventResponse();
                typedResponse.code = (Int256) eventValues.getNonIndexedValues().get(0);
                typedResponse.availAssets = (Uint256) eventValues.getNonIndexedValues().get(1);
                typedResponse.frozenAssets = (Uint256) eventValues.getNonIndexedValues().get(2);
                return typedResponse;
            }
        });
    }

    public Future<TransactionReceipt> deposit(Bytes32 uid, Uint256 assets) {
        Function function = new Function("deposit", Arrays.<Type>asList(uid, assets), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void deposit(Bytes32 uid, Uint256 assets, TransactionSucCallback callback) {
        Function function = new Function("deposit", Arrays.<Type>asList(uid, assets), Collections.<TypeReference<?>>emptyList());
        executeTransactionAsync(function, callback);
    }

    public Future<TransactionReceipt> transferInterChainCancel(Uint256 transId) {
        Function function = new Function("transferInterChainCancel", Arrays.<Type>asList(transId), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void transferInterChainCancel(Uint256 transId, TransactionSucCallback callback) {
        Function function = new Function("transferInterChainCancel", Arrays.<Type>asList(transId), Collections.<TypeReference<?>>emptyList());
        executeTransactionAsync(function, callback);
    }

    public Future<List<Type>> getHotAccoutByName(Bytes32 name) {
        Function function = new Function("getHotAccoutByName", 
                Arrays.<Type>asList(name), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        return executeCallMultipleValueReturnAsync(function);
    }

    public Future<List<Type>> getUserInfo(Bytes32 uid) {
        Function function = new Function("getUserInfo", 
                Arrays.<Type>asList(uid), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Bytes32>() {}));
        return executeCallMultipleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> register(Bytes32 uid, Uint256 assets, Uint8 identity, Bytes32 name) {
        Function function = new Function("register", Arrays.<Type>asList(uid, assets, identity, name), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void register(Bytes32 uid, Uint256 assets, Uint8 identity, Bytes32 name, TransactionSucCallback callback) {
        Function function = new Function("register", Arrays.<Type>asList(uid, assets, identity, name), Collections.<TypeReference<?>>emptyList());
        executeTransactionAsync(function, callback);
    }

    public Future<TransactionReceipt> transferInterChainByTo(Utf8String merkleRoot, Utf8String merkleProofs, Utf8String key, Utf8String value, Bytes32 from, Bytes32 to, Uint256 assets) {
        Function function = new Function("transferInterChainByTo", Arrays.<Type>asList(merkleRoot, merkleProofs, key, value, from, to, assets), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void transferInterChainByTo(Utf8String merkleRoot, Utf8String merkleProofs, Utf8String key, Utf8String value, Bytes32 from, Bytes32 to, Uint256 assets, TransactionSucCallback callback) {
        Function function = new Function("transferInterChainByTo", Arrays.<Type>asList(merkleRoot, merkleProofs, key, value, from, to, assets), Collections.<TypeReference<?>>emptyList());
        executeTransactionAsync(function, callback);
    }

    public Future<TransactionReceipt> transferInterChainConfirm(Uint256 transId) {
        Function function = new Function("transferInterChainConfirm", Arrays.<Type>asList(transId), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void transferInterChainConfirm(Uint256 transId, TransactionSucCallback callback) {
        Function function = new Function("transferInterChainConfirm", Arrays.<Type>asList(transId), Collections.<TypeReference<?>>emptyList());
        executeTransactionAsync(function, callback);
    }

    public Future<TransactionReceipt> transferInterChainByFrom(Bytes32 from, Bytes32 to, Uint256 assets) {
        Function function = new Function("transferInterChainByFrom", Arrays.<Type>asList(from, to, assets), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void transferInterChainByFrom(Bytes32 from, Bytes32 to, Uint256 assets, TransactionSucCallback callback) {
        Function function = new Function("transferInterChainByFrom", Arrays.<Type>asList(from, to, assets), Collections.<TypeReference<?>>emptyList());
        executeTransactionAsync(function, callback);
    }

    public Future<TransactionReceipt> transferOneChain(Bytes32 from, Bytes32 to, Uint256 assets) {
        Function function = new Function("transferOneChain", Arrays.<Type>asList(from, to, assets), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void transferOneChain(Bytes32 from, Bytes32 to, Uint256 assets, TransactionSucCallback callback) {
        Function function = new Function("transferOneChain", Arrays.<Type>asList(from, to, assets), Collections.<TypeReference<?>>emptyList());
        executeTransactionAsync(function, callback);
    }

    public Future<Uint256> verifySign(Utf8String hash, Utf8String pubs, Utf8String signs, Utf8String idxs) {
        Function function = new Function("verifySign", 
                Arrays.<Type>asList(hash, pubs, signs, idxs), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public static Future<Meshchain> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue) {
        return deployAsync(Meshchain.class, web3j, credentials, gasPrice, gasLimit, BINARY, "", initialWeiValue);
    }

    public static Future<Meshchain> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue) {
        return deployAsync(Meshchain.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "", initialWeiValue);
    }

    public static Meshchain load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Meshchain(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static Meshchain load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Meshchain(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static class RetLogEventResponse {
        public Int256 code;
    }

    public static class DepLogEventResponse {
        public Uint256 id;
    }

    public static class TransferLogEventResponse {
        public Uint256 id;
    }

    public static class AssetsLogEventResponse {
        public Int256 code;

        public Uint256 availAssets;

        public Uint256 frozenAssets;
    }
}
