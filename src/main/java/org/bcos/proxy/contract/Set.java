package org.bcos.proxy.contract;

import org.bcos.channel.client.TransactionSucCallback;
import org.bcos.web3j.abi.EventEncoder;
import org.bcos.web3j.abi.EventValues;
import org.bcos.web3j.abi.FunctionEncoder;
import org.bcos.web3j.abi.TypeReference;
import org.bcos.web3j.abi.datatypes.Address;
import org.bcos.web3j.abi.datatypes.Bool;
import org.bcos.web3j.abi.datatypes.DynamicArray;
import org.bcos.web3j.abi.datatypes.Event;
import org.bcos.web3j.abi.datatypes.Function;
import org.bcos.web3j.abi.datatypes.Type;
import org.bcos.web3j.abi.datatypes.Utf8String;
import org.bcos.web3j.abi.datatypes.generated.Bytes32;
import org.bcos.web3j.abi.datatypes.generated.Int256;
import org.bcos.web3j.abi.datatypes.generated.Uint256;
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
public final class Set extends Contract {
    private static final String BINARY = "6060604052341561000c57fe5b604051604080610cc2833981016040528080519060200190919080519060200190919050505b816004819055508060058190555060006006819055505b50505b610c678061005b6000396000f30060606040523615610105576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680630a9c7ca3146101075780630e0f785f1461014857806313b07915146101bd57806318b45769146101e357806339db45f61461021057806346e0c2fd1461028557806369ba1a75146102c1578063748653cf146102e1578063814844e5146103415780638f2a8abf1461037d5780639d95f1cc146103a3578063af104d1c146103d9578063b02fdbf2146103ff578063b2b99ec91461043b578063babd3d9a14610471578063d0e0ba951461049b578063e329c478146104bb578063f16a6b2c146104e1578063fc6bd6dc14610507575bfe5b341561010f57fe5b61012e600480803590602001909190803590602001909190505061052d565b604051808215151515815260200191505060405180910390f35b341561015057fe5b610158610650565b60405180806020018281038252838181518152602001915080519060200190602002808383600083146101aa575b8051825260208311156101aa57602082019150602081019050602083039250610186565b5050509050019250505060405180910390f35b34156101c557fe5b6101cd6106b3565b6040518082815260200191505060405180910390f35b34156101eb57fe5b6101f36106b9565b604051808381526020018281526020019250505060405180910390f35b341561021857fe5b6102206106cc565b6040518080602001828103825283818151815260200191508051906020019060200280838360008314610272575b8051825260208311156102725760208201915060208101905060208303925061024e565b5050509050019250505060405180910390f35b341561028d57fe5b6102a7600480803560001916906020019091905050610761565b604051808215151515815260200191505060405180910390f35b34156102c957fe5b6102df600480803590602001909190505061079a565b005b34156102e957fe5b6102ff60048080359060200190919050506107a5565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b341561034957fe5b6103636004808035600019169060200190919050506107e5565b604051808215151515815260200191505060405180910390f35b341561038557fe5b61038d6108fa565b6040518082815260200191505060405180910390f35b34156103ab57fe5b6103d7600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610900565b005b34156103e157fe5b6103e9610968565b6040518082815260200191505060405180910390f35b341561040757fe5b61041d600480803590602001909190505061096e565b60405180826000191660001916815260200191505060405180910390f35b341561044357fe5b61046f600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610993565b005b341561047957fe5b610481610af5565b604051808215151515815260200191505060405180910390f35b34156104a357fe5b6104b96004808035906020019091905050610b18565b005b34156104c357fe5b6104cb610b23565b6040518082815260200191505060405180910390f35b34156104e957fe5b6104f1610b29565b6040518082815260200191505060405180910390f35b341561050f57fe5b610517610b37565b6040518082815260200191505060405180910390f35b6000600454831015610599577f1a7562983d85e2068ef4c81643a2bf0499ea0c74d2a08ac65c332b61f8820ffd7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff6040518082815260200191505060405180910390a16000905061064a565b6005548210156105ff577f1a7562983d85e2068ef4c81643a2bf0499ea0c74d2a08ac65c332b61f8820ffd7ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe6040518082815260200191505060405180910390a161064a565b82600481905550816005819055507f1a7562983d85e2068ef4c81643a2bf0499ea0c74d2a08ac65c332b61f8820ffd60006040518082815260200191505060405180910390a1600190505b92915050565b610658610b45565b60008054806020026020016040519081016040528092919081815260200182805480156106a857602002820191906000526020600020905b81546000191681526020019060010190808311610690575b505050505090505b90565b60045481565b60006000600554600454915091505b9091565b6106d4610b59565b600280548060200260200160405190810160405280929190818152602001828054801561075657602002820191906000526020600020905b8160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001906001019080831161070c575b505050505090505b90565b600060006001600084600019166000191681526020019081526020016000205411156107905760019050610795565b600090505b919050565b806006819055505b50565b6002818154811015156107b457fe5b906000526020600020900160005b915054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b60006107f082610761565b156107fe57600190506108f5565b610806610af5565b1561081457600090506108f5565b600080548060010182816108289190610b6d565b916000526020600020900160005b849091909150906000191690555043600160008460001916600019168152602001908152602001600020819055506005546000805490501015156108f0577fc533ef893681f4b89dc58f4b3125479499e6282ff1283f3b05eb859298fa8e9660016003546040518083815260200182815260200180602001828103825260118152602001807f53455420546f756368205761726e6e756d000000000000000000000000000000815250602001935050505060405180910390a15b600190505b919050565b60035481565b600280548060010182816109149190610b99565b916000526020600020900160005b83909190916101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550505b50565b60055481565b60008181548110151561097d57fe5b906000526020600020900160005b915090505481565b6000600090505b6002805490508160ff161015610af0578173ffffffffffffffffffffffffffffffffffffffff1660028260ff168154811015156109d357fe5b906000526020600020900160005b9054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff161415610ae25760006002805490501115610acb576002600160028054905003815481101515610a4157fe5b906000526020600020900160005b9054906101000a900473ffffffffffffffffffffffffffffffffffffffff1660028260ff16815481101515610a8057fe5b906000526020600020900160005b6101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055505b6002805480919060019003610ae09190610bc5565b505b5b808060010191505061099a565b5b5050565b6000600454600080549050101515610b105760019050610b15565b600090505b90565b806003819055505b50565b60065481565b600060028054905090505b90565b600060008054905090505b90565b602060405190810160405280600081525090565b602060405190810160405280600081525090565b815481835581811511610b9457818360005260206000209182019101610b939190610bf1565b5b505050565b815481835581811511610bc057818360005260206000209182019101610bbf9190610c16565b5b505050565b815481835581811511610bec57818360005260206000209182019101610beb9190610c16565b5b505050565b610c1391905b80821115610c0f576000816000905550600101610bf7565b5090565b90565b610c3891905b80821115610c34576000816000905550600101610c1c565b5090565b905600a165627a7a723058203266075764b51cf933aa38004781fb7805280908c869de1b6f682c07c3461c620029";

    private Set(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    private Set(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static List<WarnEventResponse> getWarnEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Warn", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<WarnEventResponse> responses = new ArrayList<WarnEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            WarnEventResponse typedResponse = new WarnEventResponse();
            typedResponse.code = (Uint256) eventValues.getNonIndexedValues().get(0);
            typedResponse.setid = (Uint256) eventValues.getNonIndexedValues().get(1);
            typedResponse.msg = (Utf8String) eventValues.getNonIndexedValues().get(2);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<WarnEventResponse> warnEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Warn", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, WarnEventResponse>() {
            @Override
            public WarnEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                WarnEventResponse typedResponse = new WarnEventResponse();
                typedResponse.code = (Uint256) eventValues.getNonIndexedValues().get(0);
                typedResponse.setid = (Uint256) eventValues.getNonIndexedValues().get(1);
                typedResponse.msg = (Utf8String) eventValues.getNonIndexedValues().get(2);
                return typedResponse;
            }
        });
    }

    public static List<RetEventResponse> getRetEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Ret", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<RetEventResponse> responses = new ArrayList<RetEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            RetEventResponse typedResponse = new RetEventResponse();
            typedResponse.code = (Int256) eventValues.getNonIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<RetEventResponse> retEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Ret", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, RetEventResponse>() {
            @Override
            public RetEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                RetEventResponse typedResponse = new RetEventResponse();
                typedResponse.code = (Int256) eventValues.getNonIndexedValues().get(0);
                return typedResponse;
            }
        });
    }

    public Future<TransactionReceipt> expandSet(Uint256 max, Uint256 warn) {
        Function function = new Function("expandSet", Arrays.<Type>asList(max, warn), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void expandSet(Uint256 max, Uint256 warn, TransactionSucCallback callback) {
        Function function = new Function("expandSet", Arrays.<Type>asList(max, warn), Collections.<TypeReference<?>>emptyList());
        executeTransactionAsync(function, callback);
    }

    public Future<DynamicArray<Bytes32>> userList() {
        Function function = new Function("userList", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Bytes32>>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Uint256> m_maxnum() {
        Function function = new Function("m_maxnum", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<List<Type>> getSetCapacity() {
        Function function = new Function("getSetCapacity", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        return executeCallMultipleValueReturnAsync(function);
    }

    public Future<DynamicArray<Address>> nodeList() {
        Function function = new Function("nodeList", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Address>>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Bool> isIn(Bytes32 user) {
        Function function = new Function("isIn", 
                Arrays.<Type>asList(user), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> setStatus(Uint256 status) {
        Function function = new Function("setStatus", Arrays.<Type>asList(status), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void setStatus(Uint256 status, TransactionSucCallback callback) {
        Function function = new Function("setStatus", Arrays.<Type>asList(status), Collections.<TypeReference<?>>emptyList());
        executeTransactionAsync(function, callback);
    }

    public Future<Address> m_nodelist(Uint256 param0) {
        Function function = new Function("m_nodelist", 
                Arrays.<Type>asList(param0), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> registerRoute(Bytes32 user) {
        Function function = new Function("registerRoute", Arrays.<Type>asList(user), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void registerRoute(Bytes32 user, TransactionSucCallback callback) {
        Function function = new Function("registerRoute", Arrays.<Type>asList(user), Collections.<TypeReference<?>>emptyList());
        executeTransactionAsync(function, callback);
    }

    public Future<Uint256> m_setid() {
        Function function = new Function("m_setid", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> addNode(Address node) {
        Function function = new Function("addNode", Arrays.<Type>asList(node), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void addNode(Address node, TransactionSucCallback callback) {
        Function function = new Function("addNode", Arrays.<Type>asList(node), Collections.<TypeReference<?>>emptyList());
        executeTransactionAsync(function, callback);
    }

    public Future<Uint256> m_warnnum() {
        Function function = new Function("m_warnnum", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Bytes32> m_users(Uint256 param0) {
        Function function = new Function("m_users", 
                Arrays.<Type>asList(param0), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> removeNode(Address node) {
        Function function = new Function("removeNode", Arrays.<Type>asList(node), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void removeNode(Address node, TransactionSucCallback callback) {
        Function function = new Function("removeNode", Arrays.<Type>asList(node), Collections.<TypeReference<?>>emptyList());
        executeTransactionAsync(function, callback);
    }

    public Future<Bool> isFull() {
        Function function = new Function("isFull", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> setId(Uint256 id) {
        Function function = new Function("setId", Arrays.<Type>asList(id), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void setId(Uint256 id, TransactionSucCallback callback) {
        Function function = new Function("setId", Arrays.<Type>asList(id), Collections.<TypeReference<?>>emptyList());
        executeTransactionAsync(function, callback);
    }

    public Future<Uint256> m_status() {
        Function function = new Function("m_status", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Uint256> getNodeListNum() {
        Function function = new Function("getNodeListNum", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Uint256> getUsersNum() {
        Function function = new Function("getUsersNum", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public static Future<Set> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue, Uint256 maxnum, Uint256 warnnum) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(maxnum, warnnum));
        return deployAsync(Set.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor, initialWeiValue);
    }

    public static Future<Set> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue, Uint256 maxnum, Uint256 warnnum) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(maxnum, warnnum));
        return deployAsync(Set.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor, initialWeiValue);
    }

    public static Set load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Set(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static Set load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Set(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static class WarnEventResponse {
        public Uint256 code;

        public Uint256 setid;

        public Utf8String msg;
    }

    public static class RetEventResponse {
        public Int256 code;
    }
}
