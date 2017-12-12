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
    private static final String BINARY = "6060604052341561000f57600080fd5b604051604080610b0f83398101604052808051906020019091908051906020019091905050816004819055508060058190555060006006819055505050610ab48061005b6000396000f300606060405236156100ef576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680630e0f785f146100f457806313b079151461015e57806339db45f61461018757806346e0c2fd146101f157806369ba1a7514610230578063748653cf14610253578063814844e5146102b65780638f2a8abf146102f55780639d95f1cc1461031e578063af104d1c14610357578063b02fdbf214610380578063b2b99ec9146103bf578063babd3d9a146103f8578063d0e0ba9514610425578063e329c47814610448578063f16a6b2c14610471578063fc6bd6dc1461049a575b600080fd5b34156100ff57600080fd5b6101076104c3565b6040518080602001828103825283818151815260200191508051906020019060200280838360005b8381101561014a57808201518184015260208101905061012f565b505050509050019250505060405180910390f35b341561016957600080fd5b610171610525565b6040518082815260200191505060405180910390f35b341561019257600080fd5b61019a61052b565b6040518080602001828103825283818151815260200191508051906020019060200280838360005b838110156101dd5780820151818401526020810190506101c2565b505050509050019250505060405180910390f35b34156101fc57600080fd5b6102166004808035600019169060200190919050506105bf565b604051808215151515815260200191505060405180910390f35b341561023b57600080fd5b61025160048080359060200190919050506105f7565b005b341561025e57600080fd5b6102746004808035906020019091905050610601565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b34156102c157600080fd5b6102db600480803560001916906020019091905050610640565b604051808215151515815260200191505060405180910390f35b341561030057600080fd5b6103086106c8565b6040518082815260200191505060405180910390f35b341561032957600080fd5b610355600480803573ffffffffffffffffffffffffffffffffffffffff169060200190919050506106ce565b005b341561036257600080fd5b61036a610734565b6040518082815260200191505060405180910390f35b341561038b57600080fd5b6103a1600480803590602001909190505061073a565b60405180826000191660001916815260200191505060405180910390f35b34156103ca57600080fd5b6103f6600480803573ffffffffffffffffffffffffffffffffffffffff1690602001909190505061075e565b005b341561040357600080fd5b61040b6108ba565b604051808215151515815260200191505060405180910390f35b341561043057600080fd5b6104466004808035906020019091905050610969565b005b341561045357600080fd5b61045b610973565b6040518082815260200191505060405180910390f35b341561047c57600080fd5b610484610979565b6040518082815260200191505060405180910390f35b34156104a557600080fd5b6104ad610986565b6040518082815260200191505060405180910390f35b6104cb610992565b600080548060200260200160405190810160405280929190818152602001828054801561051b57602002820191906000526020600020905b81546000191681526020019060010190808311610503575b5050505050905090565b60045481565b6105336109a6565b60028054806020026020016040519081016040528092919081815260200182805480156105b557602002820191906000526020600020905b8160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001906001019080831161056b575b5050505050905090565b6000806001600084600019166000191681526020019081526020016000205411156105ed57600190506105f2565b600090505b919050565b8060068190555050565b60028181548110151561061057fe5b90600052602060002090016000915054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600061064b826105bf565b1561065957600190506106c3565b6106616108ba565b1561066f57600090506106c3565b6000805480600101828161068391906109ba565b9160005260206000209001600084909190915090600019169055504360016000846000191660001916815260200190815260200160002081905550600190505b919050565b60035481565b600280548060010182816106e291906109e6565b9160005260206000209001600083909190916101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055505050565b60055481565b60008181548110151561074957fe5b90600052602060002090016000915090505481565b60008090505b6002805490508160ff1610156108b6578173ffffffffffffffffffffffffffffffffffffffff1660028260ff1681548110151561079d57fe5b906000526020600020900160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1614156108a9576000600280549050111561089257600260016002805490500381548110151561080a57fe5b906000526020600020900160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1660028260ff1681548110151561084857fe5b906000526020600020900160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055505b60028054809190600190036108a79190610a12565b505b8080600101915050610764565b5050565b6000600554600080549050101515610948577fc533ef893681f4b89dc58f4b3125479499e6282ff1283f3b05eb859298fa8e9660016003546040518083815260200182815260200180602001828103825260118152602001807f53455420546f756368205761726e6e756d000000000000000000000000000000815250602001935050505060405180910390a15b6004546000805490501015156109615760019050610966565b600090505b90565b8060038190555050565b60065481565b6000600280549050905090565b60008080549050905090565b602060405190810160405280600081525090565b602060405190810160405280600081525090565b8154818355818115116109e1578183600052602060002091820191016109e09190610a3e565b5b505050565b815481835581811511610a0d57818360005260206000209182019101610a0c9190610a63565b5b505050565b815481835581811511610a3957818360005260206000209182019101610a389190610a63565b5b505050565b610a6091905b80821115610a5c576000816000905550600101610a44565b5090565b90565b610a8591905b80821115610a81576000816000905550600101610a69565b5090565b905600a165627a7a72305820476c818ea859d6a30c7414d57afb868db367f0a74d125d70442922722a4093630029";

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
}
