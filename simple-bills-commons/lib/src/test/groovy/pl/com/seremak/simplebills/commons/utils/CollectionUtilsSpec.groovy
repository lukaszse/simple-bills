package pl.com.seremak.simplebills.commons.utils

import pl.com.seremak.simplebills.commons.exceptions.DuplicatedElementsException
import pl.com.seremak.simplebills.commons.exceptions.NotFoundException
import spock.lang.Specification

class CollectionUtilsSpec extends Specification {

    def "should merge different collections correctly"() {

        expect: "should merge list correctly"
        def mergedCollection = CollectionUtils.mergeLists(list1, list2)
        mergedCollection == expectedResultAfterSortng

        where:
        list1                              | list2                            || expectedResultAfterSortng
        List.of(2, 3, 4)                   | List.of(1, 5)                    || List.of(2, 3, 4, 1, 5)
        List.of("Adam", "Marta", "Tomasz") | List.of("Krzysztof", "Adam")     || List.of("Adam", "Marta", "Tomasz", "Krzysztof", "Adam")
        List.of(BigDecimal.ONE)            | List.of(BigDecimal.valueOf(2.5)) || List.of(BigDecimal.ONE, BigDecimal.valueOf(2.5))
    }

    def "should return get single element from Collection"() {

        expect: "should return first element when collection contains only one element"
        def singleElement = CollectionUtils.getSoleElementOrThrowException(collection)
        singleElement == expectedResult

        where:
        collection                          | expectedResult
        List.of(1)                          | 1
        Set.of(2)                           | 2
        new ArrayList(List.of("first"))     | "first"
        new LinkedList<>(List.of("first"))  | "first"
        new TreeSet(Set.of(BigDecimal.ONE)) | BigDecimal.ONE
        new LinkedHashSet(Set.of(22.5))     | 22.5
        Arrays.asList(99)                   | 99
        new Vector(List.of(2))              | 2
        new PriorityQueue(List.of(3L))      | 3L
    }

    def "should throw exception when collection is empty"() {

        when: "invoke getSoleElementOrThrowException"
        CollectionUtils.getSoleElementOrThrowException(collection)

        then: "throw correct exception when collection is empty"
        def exception = thrown(NotFoundException)
        exception.getMessage() == "The searched object was not found."

        where:
        collection << [List.of(), Set.of(), Collections.emptyList(), Collections.emptySet(), new HashSet<>(), new LinkedHashSet<>()]
    }

    def "should throw exception when collection contains more than 1 element"() {

        when: "invoke getSoleElementOrThrowException"
        CollectionUtils.getSoleElementOrThrowException(collection)

        then: "throw correct exception when collection contains more than 1 element"
        def exception = thrown(DuplicatedElementsException)
        exception.getMessage() == "Internal error occurred. Multiple elements found for elements which should be unique."

        where:
        collection << [List.of(1, 2), Set.of("abc", "def"), new HashSet<>(Set.of(2, 5)), new LinkedHashSet<>(Set.of(2, "23"))]
    }
}
