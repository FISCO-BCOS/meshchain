package org.bcos.proxy.contract;

import org.bcos.channel.client.TransactionSucCallback;
import org.bcos.web3j.abi.EventEncoder;
import org.bcos.web3j.abi.EventValues;
import org.bcos.web3j.abi.FunctionEncoder;
import org.bcos.web3j.abi.TypeReference;
import org.bcos.web3j.abi.datatypes.Address;
import org.bcos.web3j.abi.datatypes.Bool;
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
public final class RouteManager extends Contract {
    private static final String BINARY = "6060604052341561000f57600080fd5b604051610f31380380610f3183398101604052808051906020019091908051820191905050816000819055508060019080519060200190610051929190610059565b5050506100fe565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061009a57805160ff19168380011785556100c8565b828001600101855582156100c8579182015b828111156100c75782518255916020019190600101906100ac565b5b5090506100d591906100d9565b5090565b6100fb91905b808211156100f75760008160009055506001016100df565b5090565b90565b610e248061010d6000396000f300606060405236156100a2576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806308ab4722146100a757806329429979146100d05780637ba9d980146100f95780637dfbc29114610187578063814844e5146101ea578063cb113bf714610229578063e50313eb14610297578063e920760014610313578063f9467b8b14610359578063faee204614610382575b600080fd5b34156100b257600080fd5b6100ba61041e565b6040518082815260200191505060405180910390f35b34156100db57600080fd5b6100e361042b565b6040518082815260200191505060405180910390f35b341561010457600080fd5b61010c610431565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561014c578082015181840152602081019050610131565b50505050905090810190601f1680156101795780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b341561019257600080fd5b6101a860048080359060200190919050506104cf565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b34156101f557600080fd5b61020f60048080356000191690602001909190505061050e565b604051808215151515815260200191505060405180910390f35b341561023457600080fd5b61024a600480803590602001909190505061095e565b60405180831515151581526020018273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019250505060405180910390f35b34156102a257600080fd5b610311600480803573ffffffffffffffffffffffffffffffffffffffff1690602001909190803590602001908201803590602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050919050506109c2565b005b341561031e57600080fd5b610338600480803560001916906020019091905050610aef565b60405180831515151581526020018281526020019250505060405180910390f35b341561036457600080fd5b61036c610bc5565b6040518082815260200191505060405180910390f35b341561038d57600080fd5b6103a36004808035906020019091905050610bcb565b6040518080602001828103825283818151815260200191508051906020019080838360005b838110156103e35780820151818401526020810190506103c8565b50505050905090810190601f1680156104105780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b6000600280549050905090565b60055481565b60018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156104c75780601f1061049c576101008083540402835291602001916104c7565b820191906000526020600020905b8154815290600101906020018083116104aa57829003601f168201915b505050505081565b6002818154811015156104de57fe5b90600052602060002090016000915054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b60008060008060046000866000191660001916815260200190815260200160002060405180807f626c6f636b6e756d626572000000000000000000000000000000000000000000815250600b0190509081526020016040518091039020541115610616577f651c611909d03094401f55cb12a30384f6d08f9e55297debdfdf531cf38aa78a600160046000876000191660001916815260200190815260200160002060405180807f7365746964000000000000000000000000000000000000000000000000000000815250600501905090815260200160405180910390205460405180831515151581526020018281526020019250505060405180910390a160019250610957565b5b600280549050600554101561090e57600260055481548110151561063757fe5b906000526020600020900160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1691508173ffffffffffffffffffffffffffffffffffffffff1663babd3d9a6000604051602001526040518163ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401602060405180830381600087803b15156106d157600080fd5b6102c65a03f115156106e257600080fd5b505050604051805190501561070857600560008154809291906001019190505550610909565b8173ffffffffffffffffffffffffffffffffffffffff1663814844e5856000604051602001526040518263ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808260001916600019168152602001915050602060405180830381600087803b151561078757600080fd5b6102c65a03f1151561079857600080fd5b5050506040518051905090508015610908574360046000866000191660001916815260200190815260200160002060405180807f626c6f636b6e756d626572000000000000000000000000000000000000000000815250600b01905090815260200160405180910390208190555060055460046000866000191660001916815260200190815260200160002060405180807f736574696400000000000000000000000000000000000000000000000000000081525060050190509081526020016040518091039020819055507f651c611909d03094401f55cb12a30384f6d08f9e55297debdfdf531cf38aa78a600160046000876000191660001916815260200190815260200160002060405180807f7365746964000000000000000000000000000000000000000000000000000000815250600501905090815260200160405180910390205460405180831515151581526020018281526020019250505060405180910390a160019250610957565b5b610617565b7f651c611909d03094401f55cb12a30384f6d08f9e55297debdfdf531cf38aa78a60008060405180831515151581526020018281526020019250505060405180910390a1600092505b5050919050565b6000806002805490508310151561097b57600080915091506109bd565b600160028481548110151561098c57fe5b906000526020600020900160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16915091505b915091565b600280548060010182816109d69190610c87565b9160005260206000209001600084909190916101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055505060038054806001018281610a399190610cb3565b916000526020600020900160008390919091509080519060200190610a5f929190610cdf565b50508173ffffffffffffffffffffffffffffffffffffffff1663d0e0ba956001600280549050036040518263ffffffff167c010000000000000000000000000000000000000000000000000000000002815260040180828152602001915050600060405180830381600087803b1515610ad757600080fd5b6102c65a03f11515610ae857600080fd5b5050505050565b600080600060046000856000191660001916815260200190815260200160002060405180807f626c6f636b6e756d626572000000000000000000000000000000000000000000815250600b0190509081526020016040518091039020541115610bb557600160046000856000191660001916815260200190815260200160002060405180807f7365746964000000000000000000000000000000000000000000000000000000815250600501905090815260200160405180910390205491509150610bc0565b600080809050915091505b915091565b60005481565b600381815481101515610bda57fe5b90600052602060002090016000915090508054600181600116156101000203166002900480601f016020809104026020016040519081016040528092919081815260200182805460018160011615610100020316600290048015610c7f5780601f10610c5457610100808354040283529160200191610c7f565b820191906000526020600020905b815481529060010190602001808311610c6257829003601f168201915b505050505081565b815481835581811511610cae57818360005260206000209182019101610cad9190610d5f565b5b505050565b815481835581811511610cda57818360005260206000209182019101610cd99190610d84565b5b505050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10610d2057805160ff1916838001178555610d4e565b82800160010185558215610d4e579182015b82811115610d4d578251825591602001919060010190610d32565b5b509050610d5b9190610d5f565b5090565b610d8191905b80821115610d7d576000816000905550600101610d65565b5090565b90565b610dad91905b80821115610da95760008181610da09190610db0565b50600101610d8a565b5090565b90565b50805460018160011615610100020316600290046000825580601f10610dd65750610df5565b601f016020900490600052602060002090810190610df49190610d5f565b5b505600a165627a7a723058201e114818da10c54b32017e5e88b57ca4e4cb44bd6a5df4732d60bea7312aa9510029";

    private RouteManager(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    private RouteManager(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static List<RegisterRetLogEventResponse> getRegisterRetLogEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("registerRetLog", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}, new TypeReference<Uint256>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<RegisterRetLogEventResponse> responses = new ArrayList<RegisterRetLogEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            RegisterRetLogEventResponse typedResponse = new RegisterRetLogEventResponse();
            typedResponse.ok = (Bool) eventValues.getNonIndexedValues().get(0);
            typedResponse.set = (Uint256) eventValues.getNonIndexedValues().get(1);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<RegisterRetLogEventResponse> registerRetLogEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("registerRetLog", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}, new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, RegisterRetLogEventResponse>() {
            @Override
            public RegisterRetLogEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                RegisterRetLogEventResponse typedResponse = new RegisterRetLogEventResponse();
                typedResponse.ok = (Bool) eventValues.getNonIndexedValues().get(0);
                typedResponse.set = (Uint256) eventValues.getNonIndexedValues().get(1);
                return typedResponse;
            }
        });
    }

    public Future<Uint256> getSetsNum() {
        Function function = new Function("getSetsNum", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Uint256> m_nowset() {
        Function function = new Function("m_nowset", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Utf8String> m_chainname() {
        Function function = new Function("m_chainname", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Address> m_sets(Uint256 param0) {
        Function function = new Function("m_sets", 
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

    public Future<List<Type>> getSetAddress(Uint256 idx) {
        Function function = new Function("getSetAddress", 
                Arrays.<Type>asList(idx), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}, new TypeReference<Address>() {}));
        return executeCallMultipleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> registerSet(Address set, Utf8String name) {
        Function function = new Function("registerSet", Arrays.<Type>asList(set, name), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void registerSet(Address set, Utf8String name, TransactionSucCallback callback) {
        Function function = new Function("registerSet", Arrays.<Type>asList(set, name), Collections.<TypeReference<?>>emptyList());
        executeTransactionAsync(function, callback);
    }

    public Future<List<Type>> getRoute(Bytes32 user) {
        Function function = new Function("getRoute", 
                Arrays.<Type>asList(user), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}, new TypeReference<Uint256>() {}));
        return executeCallMultipleValueReturnAsync(function);
    }

    public Future<Uint256> m_chainid() {
        Function function = new Function("m_chainid", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Utf8String> m_setNames(Uint256 param0) {
        Function function = new Function("m_setNames", 
                Arrays.<Type>asList(param0), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public static Future<RouteManager> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue, Uint256 chainid, Utf8String chainname) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(chainid, chainname));
        return deployAsync(RouteManager.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor, initialWeiValue);
    }

    public static Future<RouteManager> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue, Uint256 chainid, Utf8String chainname) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(chainid, chainname));
        return deployAsync(RouteManager.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor, initialWeiValue);
    }

    public static RouteManager load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new RouteManager(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static RouteManager load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new RouteManager(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static class RegisterRetLogEventResponse {
        public Bool ok;

        public Uint256 set;
    }
}
