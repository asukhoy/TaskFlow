package TaskFlow.demo;

import org.springframework.stereotype.Component;

@Component
public interface Mapper<T, U> {
    public T toDomain(U entity);
    public U toEntity(T domain);
}
