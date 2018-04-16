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
    private static final String BINARY = "6060604052341561000f57600080fd5b604051604080610ccd83398101604052808051906020019091908051906020019091905050816004819055508060058190555060006006819055505050610c728061005b6000396000f30060606040523615610105576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680630a9c7ca31461010a5780630e0f785f1461014e57806313b07915146101b857806318b45769146101e157806339db45f61461021157806346e0c2fd1461027b57806369ba1a75146102ba578063748653cf146102dd578063814844e5146103405780638f2a8abf1461037f5780639d95f1cc146103a8578063af104d1c146103e1578063b02fdbf21461040a578063b2b99ec914610449578063babd3d9a14610482578063d0e0ba95146104af578063e329c478146104d2578063f16a6b2c146104fb578063fc6bd6dc14610524575b600080fd5b341561011557600080fd5b610134600480803590602001909190803590602001909190505061054d565b604051808215151515815260200191505060405180910390f35b341561015957600080fd5b610161610670565b6040518080602001828103825283818151815260200191508051906020019060200280838360005b838110156101a4578082015181840152602081019050610189565b505050509050019250505060405180910390f35b34156101c357600080fd5b6101cb6106d2565b6040518082815260200191505060405180910390f35b34156101ec57600080fd5b6101f46106d8565b604051808381526020018281526020019250505060405180910390f35b341561021c57600080fd5b6102246106e9565b6040518080602001828103825283818151815260200191508051906020019060200280838360005b8381101561026757808201518184015260208101905061024c565b505050509050019250505060405180910390f35b341561028657600080fd5b6102a060048080356000191690602001909190505061077d565b604051808215151515815260200191505060405180910390f35b34156102c557600080fd5b6102db60048080359060200190919050506107b5565b005b34156102e857600080fd5b6102fe60048080359060200190919050506107bf565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b341561034b57600080fd5b6103656004808035600019169060200190919050506107fe565b604051808215151515815260200191505060405180910390f35b341561038a57600080fd5b610392610886565b6040518082815260200191505060405180910390f35b34156103b357600080fd5b6103df600480803573ffffffffffffffffffffffffffffffffffffffff1690602001909190505061088c565b005b34156103ec57600080fd5b6103f46108f2565b6040518082815260200191505060405180910390f35b341561041557600080fd5b61042b60048080359060200190919050506108f8565b60405180826000191660001916815260200191505060405180910390f35b341561045457600080fd5b610480600480803573ffffffffffffffffffffffffffffffffffffffff1690602001909190505061091c565b005b341561048d57600080fd5b610495610a78565b604051808215151515815260200191505060405180910390f35b34156104ba57600080fd5b6104d06004808035906020019091905050610b27565b005b34156104dd57600080fd5b6104e5610b31565b6040518082815260200191505060405180910390f35b341561050657600080fd5b61050e610b37565b6040518082815260200191505060405180910390f35b341561052f57600080fd5b610537610b44565b6040518082815260200191505060405180910390f35b60006004548310156105b9577f1a7562983d85e2068ef4c81643a2bf0499ea0c74d2a08ac65c332b61f8820ffd7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff6040518082815260200191505060405180910390a16000905061066a565b60055482101561061f577f1a7562983d85e2068ef4c81643a2bf0499ea0c74d2a08ac65c332b61f8820ffd7ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe6040518082815260200191505060405180910390a161066a565b82600481905550816005819055507f1a7562983d85e2068ef4c81643a2bf0499ea0c74d2a08ac65c332b61f8820ffd60006040518082815260200191505060405180910390a1600190505b92915050565b610678610b50565b60008054806020026020016040519081016040528092919081815260200182805480156106c857602002820191906000526020600020905b815460001916815260200190600101908083116106b0575b5050505050905090565b60045481565b600080600554600454915091509091565b6106f1610b64565b600280548060200260200160405190810160405280929190818152602001828054801561077357602002820191906000526020600020905b8160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019060010190808311610729575b5050505050905090565b6000806001600084600019166000191681526020019081526020016000205411156107ab57600190506107b0565b600090505b919050565b8060068190555050565b6002818154811015156107ce57fe5b90600052602060002090016000915054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b60006108098261077d565b156108175760019050610881565b61081f610a78565b1561082d5760009050610881565b600080548060010182816108419190610b78565b9160005260206000209001600084909190915090600019169055504360016000846000191660001916815260200190815260200160002081905550600190505b919050565b60035481565b600280548060010182816108a09190610ba4565b9160005260206000209001600083909190916101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055505050565b60055481565b60008181548110151561090757fe5b90600052602060002090016000915090505481565b60008090505b6002805490508160ff161015610a74578173ffffffffffffffffffffffffffffffffffffffff1660028260ff1681548110151561095b57fe5b906000526020600020900160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff161415610a675760006002805490501115610a505760026001600280549050038154811015156109c857fe5b906000526020600020900160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1660028260ff16815481101515610a0657fe5b906000526020600020900160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055505b6002805480919060019003610a659190610bd0565b505b8080600101915050610922565b5050565b6000600554600080549050101515610b06577fc533ef893681f4b89dc58f4b3125479499e6282ff1283f3b05eb859298fa8e9660016003546040518083815260200182815260200180602001828103825260118152602001807f53455420546f756368205761726e6e756d000000000000000000000000000000815250602001935050505060405180910390a15b600454600080549050101515610b1f5760019050610b24565b600090505b90565b8060038190555050565b60065481565b6000600280549050905090565b60008080549050905090565b602060405190810160405280600081525090565b602060405190810160405280600081525090565b815481835581811511610b9f57818360005260206000209182019101610b9e9190610bfc565b5b505050565b815481835581811511610bcb57818360005260206000209182019101610bca9190610c21565b5b505050565b815481835581811511610bf757818360005260206000209182019101610bf69190610c21565b5b505050565b610c1e91905b80821115610c1a576000816000905550600101610c02565b5090565b90565b610c4391905b80821115610c3f576000816000905550600101610c27565b5090565b905600a165627a7a72305820f564be2534faed537cfa22f7c4d75a1b6517366d2644519bbbeedb8f9e21e13f0029";

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
