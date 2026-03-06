package guru.qa.niffler.api.core.converter;


import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class SoapConverterFactory extends Converter.Factory {
    static final MediaType XML = MediaType.get("application/xml; charset=utf-8");

    /** Create an instance using a default {@link JAXBContext} instance for conversion. */
    public static SoapConverterFactory create(@Nonnull String namespace) {
        return new SoapConverterFactory(null, namespace);
    }

    /** Create an instance using {@code context} for conversion. */
    @SuppressWarnings("ConstantConditions") // Guarding public API nullability.
    public static SoapConverterFactory create(@Nonnull JAXBContext context, @Nonnull String namespace) {
        if (context == null) throw new NullPointerException("context == null");
        return new SoapConverterFactory(context, namespace);
    }

    /** If null, a new JAXB context will be created for each type to be converted. */
    private final @Nullable JAXBContext context;
    private final @Nonnull String namespace;

    private SoapConverterFactory(@Nullable JAXBContext context, @Nonnull String namespace) {
        this.context = context;
        this.namespace = namespace;
    }

    @Override
    public @Nullable Converter<?, RequestBody> requestBodyConverter(
            Type type,
            Annotation[] parameterAnnotations,
            Annotation[] methodAnnotations,
            Retrofit retrofit) {
        if (type instanceof Class && ((Class<?>) type).isAnnotationPresent(XmlRootElement.class)) {
            return new SoapRequestConverter<>(contextForType((Class<?>) type), namespace);
        }
        return null;
    }

    @Override
    public @Nullable Converter<ResponseBody, ?> responseBodyConverter(
            Type type, Annotation[] annotations, Retrofit retrofit) {
        if (type instanceof Class && ((Class<?>) type).isAnnotationPresent(XmlRootElement.class)) {
            return new SoapResponseConverter<>(contextForType((Class<?>) type), (Class<?>) type);
        }
        return null;
    }

    private JAXBContext contextForType(Class<?> type) {
        try {
            return context != null ? context : JAXBContext.newInstance(type);
        } catch (JAXBException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
